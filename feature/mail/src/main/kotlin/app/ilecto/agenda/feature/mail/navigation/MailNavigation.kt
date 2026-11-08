package app.ilecto.agenda.feature.mail.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.ilecto.agenda.feature.mail.MailSuggestionsRoute

const val MAIL_ROUTE = "mail"

fun NavGraphBuilder.mailSuggestionsScreen() {
    composable(MAIL_ROUTE) { MailSuggestionsRoute() }
}
