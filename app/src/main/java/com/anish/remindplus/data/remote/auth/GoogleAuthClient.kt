package com.anish.remindplus.data.remote.auth

import android.content.Context
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

class GoogleAuthClient(
    context: Context
) {

    val oneTapClient: SignInClient =
        Identity.getSignInClient(context)
}