import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CurrencyConverter {

    BigDecimal amountFrom, amountTo;
    String initialCurrency, convertedCurrency;
    Scanner textInput;
    private String API_KEY;



    public CurrencyConverter(){
    }

    public static void main(String[] args) throws IOException {
        CurrencyConverter myConverter = new CurrencyConverter();

        myConverter.getAPI_KEY();
        myConverter.receiveInput();
        myConverter.getRates();

    }

    private String getAPI_KEY(){

        try {
            File myFileObj = new File("Additional Files\\APIkey.txt");
            Scanner myReader = new Scanner(myFileObj);
            while (myReader.hasNextLine()) {
                API_KEY = myReader.nextLine();
                System.out.println("API KEY: "+API_KEY);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        return API_KEY;
    }

    private void receiveInput(){
        this.textInput = new Scanner(System.in);
        System.out.println("Please enter a currency code: ");
        initialCurrency = textInput.nextLine().toUpperCase();
        System.out.println(initialCurrency);

        System.out.println("Enter currency code you are converting to: ");
        convertedCurrency = textInput.nextLine().toUpperCase();

        System.out.println("You are converting from "+initialCurrency+ " to "+convertedCurrency+".");
        System.out.println("Please enter an amount: ");
        amountFrom = textInput.nextBigDecimal();
        System.out.println(amountFrom);

    }

    private void getRates() throws IOException {


        URL url = new URL("https://api.exchangeratesapi.io/v1/latest" +
                "?access_key=" + API_KEY +
                "&base=EUR" +
                "&symbols=" +initialCurrency+","+convertedCurrency);

        System.out.println(url);

        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("GET");
        httpConnection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

        try(BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream())))
        {
            StringBuilder jsonString = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                jsonString.append(inputLine);
                System.out.println(inputLine);

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonString.toString(), JsonObject.class);

                if (jsonObject.get("success").getAsBoolean()) {
                    JsonObject ratesObject = jsonObject.getAsJsonObject("rates");

                    float fromRate = ratesObject.get(initialCurrency).getAsFloat();
                    float toRate = ratesObject.get(convertedCurrency).getAsFloat();

                    System.out.println("fromRate: " + fromRate);
                    System.out.println("toRate: " + toRate);

                    calculateConvertedValue(fromRate, toRate);

                } else {
                    System.out.println("Error, API request failed.");
                }
            }
        }

    }

    private void calculateConvertedValue(float fromRate, float toRate){

        float conversionRateFloat = toRate/fromRate;
        BigDecimal conversionRate = new BigDecimal(conversionRateFloat);
        amountTo = amountFrom.multiply(conversionRate).setScale(2, RoundingMode.HALF_UP);

        System.out.println("Rate of "+initialCurrency+" to "+convertedCurrency+": "+conversionRate);
        System.out.println(amountTo);

    }
}
