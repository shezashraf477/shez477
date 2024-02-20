import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EnricherManager @Autowired()(enrichers: List[MessageEnricher]) {
  // Logic to find the correct enricher based on the incoming message
  def enrich(message: String): String = {
    // Find the enricher that can handle the message
    val enricherOption = enrichers.find(_.canEnrich(message))
    
    // Enrich the message using the selected enricher
    enricherOption match {
      case Some(enricher) => enricher.enrich(message)
      case None => throw new RuntimeException("No enricher found for the message")
    }
  }
}
