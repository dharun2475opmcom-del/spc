package com.college.parking;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
 * Handles form-urlencoded POST requests for /api/auth/signup and /api/auth/login.
 */
@WebServlet(urlPatterns = {"/api/auth/signup", "/api/auth/login"})
public class AuthServlet extends HttpServlet {
    private static final int BCRYPT_COST = 12;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");

        String path = request.getServletPath();
        try {
            if ("/api/auth/signup".equals(path)) {
                signup(request, response);
            } else if ("/api/auth/login".equals(path)) {
                login(request, response);
            } else {
                sendJson(response, HttpServletResponse.SC_NOT_FOUND, false, "Endpoint not found.");
            }
        } catch (SQLException exception) {
            getServletContext().log("Database error in authentication endpoint.", exception);
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false,
                "The database could not complete the request. Check the database setup and server log.");
        } catch (IllegalArgumentException exception) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false, exception.getMessage());
        }
    }

    private void signup(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String name = trim(request.getParameter("userName"));
        String email = normalizeEmail(request.getParameter("email"));
        String contactInfo = trim(request.getParameter("contactInfo"));
        String vehicleNumber = trim(request.getParameter("vehicleNumber")).toUpperCase(Locale.ROOT);
        String password = request.getParameter("password");

        if (name.isBlank() || email.isBlank() || contactInfo.isBlank()
                || vehicleNumber.isBlank() || password == null || password.length() < 8) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false,
                "Complete every field. Password must contain at least 8 characters.");
            return;
        }
        if (name.length() > 100 || email.length() > 190 || contactInfo.length() > 30
                || vehicleNumber.length() > 20) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false,
                "One or more fields are too long.");
            return;
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false,
                "Password must be no longer than 72 UTF-8 bytes.");
            return;
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false,
                "Enter a valid email address.");
            return;
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
        String sql = "INSERT INTO users (user_name, email, contact_info, vehicle_number, password_hash) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, contactInfo);
            statement.setString(4, vehicleNumber);
            statement.setString(5, passwordHash);

            int inserted = statement.executeUpdate();
            if (inserted != 1) {
                sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false,
                    "Account could not be created. Please try again.");
                return;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    HttpSession session = request.getSession(true);
                    request.changeSessionId();
                    session.setAttribute("userId", keys.getLong(1));
                    session.setAttribute("userName", name);
                }
            }
            sendJson(response, HttpServletResponse.SC_CREATED, true,
                "Account created successfully. You can now continue to the parking dashboard.");
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                sendJson(response, HttpServletResponse.SC_CONFLICT, false,
                    "An account with this email may already exist.");
                return;
            }
            throw exception;
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String email = normalizeEmail(request.getParameter("email"));
        String password = request.getParameter("password");

        if (email.isBlank() || password == null || password.isBlank()) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST, false,
                "Enter your email and password.");
            return;
        }

        String sql = "SELECT user_id, user_name, password_hash FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, false,
                        "Email or password is incorrect.");
                    return;
                }

                String storedHash = result.getString("password_hash");
                if (storedHash == null || storedHash.isBlank() || !BCrypt.checkpw(password, storedHash)) {
                    sendJson(response, HttpServletResponse.SC_UNAUTHORIZED, false,
                        "Email or password is incorrect.");
                    return;
                }

                long userId = result.getLong("user_id");
                String userName = result.getString("user_name");

                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }
                HttpSession session = request.getSession(true);
                request.changeSessionId();
                session.setAttribute("userId", userId);
                session.setAttribute("userName", userName);

                sendJson(response, HttpServletResponse.SC_OK, true, "Welcome back, " + userName + "!");
            }
        }
    }

    private static String normalizeEmail(String value) {
        return trim(value).toLowerCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static void sendJson(HttpServletResponse response, int status, boolean success, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"success\":" + success + ",\"message\":\"" + jsonEscape(message) + "\"}");
    }

    private static String jsonEscape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
