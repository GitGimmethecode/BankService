import java.util.HashMap;
import java.util.Map;

public class CurrencyService {

    private Map<String, Double> rates = new HashMap<>();

    public CurrencyService() {
        rates.put("UAH", 1.0);
        rates.put("USD", 41.26);
        rates.put("EUR", 48.23);
    }

    public double getRate(String currency) {
        if (!rates.containsKey(currency)) {
            throw new IllegalArgumentException("Rates for currency " + currency + " not found");
        }
        return rates.get(currency);
    }

    public double convert(String fromCurrency, String toCurrency, double amount) {
        double fromRate = getRate(fromCurrency);
        double toRate = getRate(toCurrency);

        double result = amount * (fromRate / toRate);
        return Math.round(result * 100.0) / 100.0;
    }

    public Map<String, Double> getRates() {
        return rates;
    }
}