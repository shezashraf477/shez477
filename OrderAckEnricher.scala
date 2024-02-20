@Component
class OrderEnricher extends MessageEnricher {
  override def canEnrich(message: String): Boolean = {
    // Check if the message contains keywords related to orders
    val keywords = Seq("order", "purchase", "transaction")
    keywords.exists(message.toLowerCase.contains)
  }
  
  override def enrich(message: String): String = {
    // Logic to enrich the message for orders
    // This method will be called only if canEnrich returns true for the message
  }
}
