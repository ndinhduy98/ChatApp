package com.freezer.chatapp.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val phoneNumberError: Int? = null,
    val smsCodeError: Int? = null,
    val isDataValid: Boolean = false
)