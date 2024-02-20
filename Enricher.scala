import org.springframework.stereotype.Component

trait Enricher {
  def canEnrich(message: String): Boolean
  def enrich(message: String): String
}

@Component
class OrderEnricher extends Enricher {
  override def canEnrich(message: String): Boolean = message.contains("ORDER")
  override def enrich(message: String): String = "Enriched Order Message"
}

@Component
class PaymentEnricher extends Enricher {
  override def canEnrich(message: String): Boolean = message.contains("PAYMENT")
  override def enrich(message: String): String = "Enriched Payment Message"
}
