function togglePassword() {
    const input = document.getElementById("password");
    const icon = document.getElementById("toggleIconReg");
    if (input.type === "password") {
        input.type = "text";
        icon.classList.replace("fa-eye", "fa-eye-slash");
        icon.setAttribute('aria-label', 'Hide password');
    } else {
        input.type = "password";
        icon.classList.replace("fa-eye-slash", "fa-eye");
        icon.setAttribute('aria-label', 'Show password');
    }
}

function togglePasswordRepeat() {
    const input = document.getElementById("repeat_password");
    const icon = document.getElementById("toggleIconRegRepeat");
    if (input.type === "password") {
        input.type = "text";
        icon.classList.replace("fa-eye", "fa-eye-slash");
        icon.setAttribute('aria-label', 'Hide password');
    } else {
        input.type = "password";
        icon.classList.replace("fa-eye-slash", "fa-eye");
        icon.setAttribute('aria-label', 'Show password');
    }
}