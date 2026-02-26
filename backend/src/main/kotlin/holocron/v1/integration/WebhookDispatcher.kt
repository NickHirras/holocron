package holocron.v1.integration

import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.MediaType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import java.net.URI

object WebhookDispatcher {
    
    enum class EventType {
        CEREMONY_STARTED,
        RESPONSE_SUBMITTED,
        CEREMONY_COMPLETED
    }

    data class WebhookContext(
        val templateId: String,
        val templateTitle: String,
        val teamName: String,
        val userEmail: String? = null,
        val totalResponses: Int = 0,
        val totalMembers: Int = 0,
        val frontendUrl: String = "http://localhost:4200"
    )

    private val client = WebClient.of()

    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(urls: List<String>, event: EventType, context: WebhookContext) {
        if (urls.isEmpty()) return

        GlobalScope.launch {
            urls.forEach { url ->
                try {
                    val platform = detectPlatform(url)
                    val jsonPayload = buildPayload(platform, event, context)
                    
                    println("🌐 Dispatching $platform webhook for ${event.name} to $url")
                    val req = HttpRequest.builder()
                        .post(url)
                        .content(MediaType.JSON_UTF_8, jsonPayload)
                        .build()
                        
                    val response = client.execute(req)
                    response.aggregate().await()
                    println("✅ Webhook to $url dispatched successfully.")
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    println("❌ Failed to dispatch webhook to $url: \${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private enum class Platform {
        GOOGLE_CHAT, SLACK, MS_TEAMS, GENERIC
    }

    private fun detectPlatform(url: String): Platform {
        return when {
            url.contains("chat.googleapis.com") -> Platform.GOOGLE_CHAT
            url.contains("hooks.slack.com") -> Platform.SLACK
            url.contains("webhook.office.com") -> Platform.MS_TEAMS
            else -> Platform.GENERIC
        }
    }

    private fun buildPayload(platform: Platform, event: EventType, context: WebhookContext): String {
        val (title, text, buttonUrl, buttonText) = when (event) {
            EventType.CEREMONY_STARTED -> 
                listOf("Ceremony Ready", "The ceremony **${context.templateTitle}** for team **${context.teamName}** is now ready for your input!", "${context.frontendUrl}/ceremony/${context.templateId}", "Respond Now")
            EventType.RESPONSE_SUBMITTED -> {
                val userStr = if (context.userEmail != null && context.userEmail != "anonymous") " by ${context.userEmail}" else ""
                listOf("New Response", "A new response was submitted for **${context.templateTitle}**$userStr. (${context.totalResponses}/${context.totalMembers} completed)", "${context.frontendUrl}/dashboard", "View Dashboard")
            }
            EventType.CEREMONY_COMPLETED -> 
                listOf("Ceremony Complete! \uD83C\uDF89", "All ${context.totalMembers} members of **${context.teamName}** have submitted their responses for **${context.templateTitle}**.", "${context.frontendUrl}/dashboard", "View Results")
        }

        return when (platform) {
            Platform.GOOGLE_CHAT -> """
                {
                  "cardsV2": [
                    {
                      "cardId": "holocron-card-\${System.currentTimeMillis()}",
                      "card": {
                        "header": {
                          "title": "Holocron: $title",
                          "subtitle": "Holocron Systems"
                        },
                        "sections": [
                          {
                            "widgets": [
                              {
                                "textParagraph": {
                                  "text": "$text"
                                }
                              },
                              {
                                "buttonList": {
                                  "buttons": [
                                    {
                                      "text": "$buttonText",
                                      "onClick": {
                                        "openLink": {
                                          "url": "$buttonUrl"
                                        }
                                      }
                                    }
                                  ]
                                }
                              }
                            ]
                          }
                        ]
                      }
                    }
                  ]
                }
            """.trimIndent()
            
            Platform.SLACK -> """
                {
                    "blocks": [
                        {
                            "type": "header",
                            "text": {
                                "type": "plain_text",
                                "text": "Holocron: $title",
                                "emoji": true
                            }
                        },
                        {
                            "type": "section",
                            "text": {
                                "type": "mrkdwn",
                                "text": "$text"
                            },
                            "accessory": {
                                "type": "button",
                                "text": {
                                    "type": "plain_text",
                                    "text": "$buttonText",
                                    "emoji": true
                                },
                                "value": "click_me_123",
                                "url": "$buttonUrl",
                                "action_id": "button-action"
                            }
                        }
                    ]
                }
            """.trimIndent()
            
            Platform.MS_TEAMS -> """
                {
                    "type": "message",
                    "attachments": [
                        {
                            "contentType": "application/vnd.microsoft.card.adaptive",
                            "contentUrl": null,
                            "content": {
                                "${"$"}schema": "http://adaptivecards.io/schemas/adaptive-card.json",
                                "type": "AdaptiveCard",
                                "version": "1.4",
                                "body": [
                                    {
                                        "type": "TextBlock",
                                        "text": "Holocron: $title",
                                        "weight": "Bolder",
                                        "size": "Medium"
                                    },
                                    {
                                        "type": "TextBlock",
                                        "text": "$text",
                                        "wrap": true
                                    }
                                ],
                                "actions": [
                                    {
                                        "type": "Action.OpenUrl",
                                        "title": "$buttonText",
                                        "url": "$buttonUrl"
                                    }
                                ]
                            }
                        }
                    ]
                }
            """.trimIndent()
            
            Platform.GENERIC -> """
                {
                    "event": "${event.name.lowercase()}",
                    "title": "$title",
                    "text": "$text",
                    "template_id": "${context.templateId}",
                    "url": "$buttonUrl"
                }
            """.trimIndent()
        }
    }
}
