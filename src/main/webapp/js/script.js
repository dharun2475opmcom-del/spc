const loginTab = document.getElementById("login-tab");
const signupTab = document.getElementById("signup-tab");
const authTitle = document.getElementById("auth-title");
const authDescription = document.getElementById("auth-description");
const submitButton = document.getElementById("submit-button");
const nameField = document.getElementById("name-field");
const phoneField = document.getElementById("phone-field");
const vehicleField = document.getElementById("vehicle-field");
const nameInput = document.getElementById("user-name");
const phoneInput = document.getElementById("contact-info");
const vehicleInput = document.getElementById("vehicle-number");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const message = document.getElementById("form-message");
const authForm = document.getElementById("auth-form");

let mode = "login";
let isSubmitting = false;

function setMode(nextMode) {
    mode = nextMode;
    const isSignup = mode === "signup";

    loginTab.classList.toggle("active", !isSignup);
    signupTab.classList.toggle("active", isSignup);
    loginTab.setAttribute("aria-selected", String(!isSignup));
    signupTab.setAttribute("aria-selected", String(isSignup));

    nameField.hidden = !isSignup;
    phoneField.hidden = !isSignup;
    vehicleField.hidden = !isSignup;

    nameInput.required = isSignup;
    phoneInput.required = isSignup;
    vehicleInput.required = isSignup;
    passwordInput.autocomplete = isSignup ? "new-password" : "current-password";
    passwordInput.minLength = isSignup ? 8 : 1;

    authTitle.textContent = isSignup ? "Create your account" : "Welcome back";
    authDescription.textContent = isSignup
        ? "Register to start reserving campus parking."
        : "Sign in to manage your parking reservations.";
    submitButton.innerHTML = isSignup
        ? 'Create account <span aria-hidden="true">→</span>'
        : 'Sign in <span aria-hidden="true">→</span>';

    message.textContent = "";
    message.style.color = "#b54848";
}

function showMessage(text, isSuccess = false) {
    message.textContent = text;
    message.style.color = isSuccess ? "#28795e" : "#b54848";
}

loginTab.addEventListener("click", () => setMode("login"));
signupTab.addEventListener("click", () => setMode("signup"));

authForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (isSubmitting) return;
    if (!authForm.checkValidity()) {
        authForm.reportValidity();
        return;
    }

    isSubmitting = true;
    submitButton.disabled = true;
    submitButton.setAttribute("aria-busy", "true");
    submitButton.textContent = mode === "signup" ? "Creating account..." : "Signing in...";
    showMessage("");

    const formData = new URLSearchParams();
    formData.set("email", emailInput.value.trim());
    formData.set("password", passwordInput.value);

    if (mode === "signup") {
        formData.set("userName", nameInput.value.trim());
        formData.set("contactInfo", phoneInput.value.trim());
        formData.set("vehicleNumber", vehicleInput.value.trim());
    }

    try {
        const endpoint = mode === "signup" ? "api/auth/signup" : "api/auth/login";
        const response = await fetch(endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: formData.toString(),
            credentials: "same-origin"
        });

        let result;
        try {
            result = await response.json();
        } catch {
            throw new Error("The server returned an unexpected response. Check that Tomcat is running and the app is deployed.");
        }

        if (!response.ok || !result.success) {
            showMessage(result.message || "The request could not be completed.");
            return;
        }

        showMessage(result.message, true);
        if (mode === "signup") {
            authTitle.textContent = "Account created!";
            authDescription.textContent = "Your account is ready. The parking dashboard will be the next project step.";
        }
    } catch (error) {
        showMessage(error.message || "Could not connect to the server. Check that the application is running.");
    } finally {
        isSubmitting = false;
        submitButton.disabled = false;
        submitButton.removeAttribute("aria-busy");
        submitButton.innerHTML = mode === "signup"
            ? 'Create account <span aria-hidden="true">→</span>'
            : 'Sign in <span aria-hidden="true">→</span>';
    }
});
