package holocron.v1.integration

object EmailDispatcher {
    fun dispatch(emails: List<String>, subject: String, body: String) {
        if (emails.isEmpty()) return
        emails.forEach { email ->
            println("=========================================")
            println("📧 EMAIL DISPATCHED")
            println("To: $email")
            println("Subject: $subject")
            println("-----------------------------------------")
            println(body)
            println("=========================================")
        }
    }
}
