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
const passwordInput = document.getElementById("password");
const message = document.getElementById("form-message");
const authForm = document.getElementById("auth-form");

let mode = "login";

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

    authTitle.textContent = isSignup ? "Create your account" : "Welcome back";
    authDescription.textContent = isSignup
        ? "Register to start reserving campus parking."
        : "Sign in to manage your parking reservations.";
    submitButton.innerHTML = isSignup
        ? 'Create account <span aria-hidden="true">→</span>'
        : 'Sign in <span aria-hidden="true">→</span>';

    message.textContent = "";
}

loginTab.addEventListener("click", () => setMode("login"));
signupTab.addEventListener("click", () => setMode("signup"));

authForm.addEventListener("submit", (event) => {
    event.preventDefault();
    message.style.color = "#b54848";

    if (!authForm.checkValidity()) {
        authForm.reportValidity();
        return;
    }

    message.style.color = "#71808c";
    message.textContent = mode === "signup"
        ? "The form is valid. Account creation will be connected to the Java backend in the next stage."
        : "The form is valid. Sign-in will be connected to the Java backend in the next stage.";
});
