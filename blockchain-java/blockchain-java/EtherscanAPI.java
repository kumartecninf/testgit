import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class EtherscanAPI{
    public static void main(String[] args) {
        HttpClient httpClient = HttpClients.createDefault();
        Gson gson = new Gson();

        // Etherscan API endpoint
        String etherscanApiUrl = "https://api.etherscan.io/api";

        // Your Etherscan API key
        String apiKey = "NZK1YH5SNM9CGVJPF1TQAREYUWHGRUJWMN";

        // Wallet address for which you want to retrieve addresses
        String walletAddress = "0x690b9a9e9aa1c9db991c7721a92d351db4fac990";

        // Get Transaction Details
        String transactionEndpoint = etherscanApiUrl + "?module=account&action=txlist&address=" + walletAddress + "&apikey=" + apiKey;
        HttpGet transactionRequest = new HttpGet(transactionEndpoint);
        try {
            HttpResponse transactionResponse = httpClient.execute(transactionRequest);
            if (transactionResponse.getStatusLine().getStatusCode() == 200) {
                String transactionJson = EntityUtils.toString(transactionResponse.getEntity());
                TransactionData transactionData = gson.fromJson(transactionJson, TransactionData.class);
                System.out.println("Tx Data: " + transactionJson);
                System.out.println("Tx Data: " + transactionData);
            } else {
                System.out.println("Failed to retrieve account balance.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get Account Balance
        String balanceEndpoint = etherscanApiUrl + "?module=account&action=balance&address=" + walletAddress + "&apikey=" + apiKey;
        HttpGet balanceRequest = new HttpGet(balanceEndpoint);
        try {
            HttpResponse balanceResponse = httpClient.execute(balanceRequest);
            if (balanceResponse.getStatusLine().getStatusCode() == 200) {
                String balanceJson = EntityUtils.toString(balanceResponse.getEntity());
                BalanceData balanceData = gson.fromJson(balanceJson, BalanceData.class);
                System.out.println("Account Balance: " + balanceData + balanceJson);
            } else {
                System.out.println("Failed to retrieve account balance.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get Account Balance for multiple addresses
        String multiBalanceEndpoint = etherscanApiUrl + "?module=account&action=balancemulti&address=" + walletAddress + "&apikey=" + apiKey;
        HttpGet multiBalanceRequest = new HttpGet(multiBalanceEndpoint);
        try {
            HttpResponse multiBalanceResponse = httpClient.execute(multiBalanceRequest);
            if (multiBalanceResponse.getStatusLine().getStatusCode() == 200) {
                String multiBalanceJson = EntityUtils.toString(multiBalanceResponse.getEntity());
                MultiBalanceData multiBalanceData = gson.fromJson(multiBalanceJson, MultiBalanceData.class);
                System.out.println("Account Balance (Multiple): " + multiBalanceData);
            } else {
                System.out.println("Failed to retrieve account balance.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get Token Balance
        String tokenContractAddress = "0x388c818ca8b9251b393131c08a736a67ccb19297";
        String tokenBalanceEndpoint = etherscanApiUrl + "?module=account&action=tokenbalance&contractaddress=" + tokenContractAddress + "&address=" + walletAddress + "&apikey=" + apiKey;
        HttpGet tokenBalanceRequest = new HttpGet(tokenBalanceEndpoint);
        try {
            HttpResponse tokenBalanceResponse = httpClient.execute(tokenBalanceRequest);
            if (tokenBalanceResponse.getStatusLine().getStatusCode() == 200) {
                String tokenBalanceJson = EntityUtils.toString(tokenBalanceResponse.getEntity());
                TokenBalanceData tokenBalanceData = gson.fromJson(tokenBalanceJson, TokenBalanceData.class);
                System.out.println("Token Balance: " + tokenBalanceData);
            } else {
                System.out.println("Failed to retrieve token balance.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get Ether Supply
        String etherSupplyEndpoint = etherscanApiUrl + "?module=stats&action=ethsupply&apikey=" + apiKey;
        HttpGet etherSupplyRequest = new HttpGet(etherSupplyEndpoint);
        try {
            HttpResponse etherSupplyResponse = httpClient.execute(etherSupplyRequest);
            if (etherSupplyResponse.getStatusLine().getStatusCode() == 200) {
                String etherSupplyJson = EntityUtils.toString(etherSupplyResponse.getEntity());
                EtherSupplyData etherSupplyData = gson.fromJson(etherSupplyJson, EtherSupplyData.class);
                System.out.println("Ether Supply: " + etherSupplyData);
            } else {
                System.out.println("Failed to get Ether Supply.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class TransactionData {
    // Define the fields based on the expected JSON response structure
    // Example: "status": "1", "result": [...]
    // You can customize this class based on your specific JSON structure
    String status;
    String message;
    Transaction[] result;

    // Inner class representing a single transaction
    static class Transaction {
        // Define the fields for each transaction based on the expected JSON structure
        // Example: "hash", "from", "to", "value", etc.
        // You can customize this class based on your specific JSON structure
        String hash;
        String from;
        String to;
        String value;
    }
}

class BalanceData {
    // Define the fields based on the expected JSON response structure
    // Example: "status": "1", "message": "OK", "result": "1000000000000000000"
    // You can customize this class based on your specific JSON structure
    String status;
    String message;
    String result;
}

class MultiBalanceData {
    // Define the fields based on the expected JSON response structure
    // Example: "status": "1", "message": "OK", "result": [{...}, {...}]
    // You can customize this class based on your specific JSON structure
    String status;
    String message;
    MultiBalance[] result;

    // Inner class representing a single account balance
    static class MultiBalance {
        // Define the fields for each account balance based on the expected JSON structure
        // Example: "account", "balance", etc.
        // You can customize this class based on your specific JSON structure
        String account;
        String balance;
    }
}

class TokenBalanceData {
    // Define the fields based on the expected JSON response structure
    // Example: "status": "1", "message": "OK", "result": "1000000000000000000"
    // You can customize this class based on your specific JSON structure
    String status;
    String message;
    String result;
}

class EtherSupplyData {
    // Define the fields based on the expected JSON response structure
    // Example: "status": "1", "message": "OK", "result": "1000000000000000000"
    // You can customize this class based on your specific JSON structure
    String status;
    String message;
    String result;
}
