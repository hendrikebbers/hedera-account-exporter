package de.devlodge.hedera.account.export.clients;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.devlodge.hedera.account.export.models.HederaTransaction;
import de.devlodge.hedera.account.export.models.HederaTransfer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class HederaClient {
    public static final String GET_TRANSACTION_URL = "https://mainnet-public.mirrornode.hedera.com/api/v1/transactions?account.id=%s&order=asc&transactiontype=CRYPTOTRANSFER&result=success";
    private final HttpClient client;

    public HederaClient() {
        client = HttpClient.newHttpClient();
    }

    public List<HederaTransaction> request(final String accountId) throws IOException, InterruptedException {
        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(GET_TRANSACTION_URL.formatted(accountId)))
                .header("accept", "application/json");

        final HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        return JsonParser.parseString(response.body())
                .getAsJsonObject()
                .get("transactions")
                .getAsJsonArray()
                .asList()
                .stream()
                .map(transaction -> {
                    final List<HederaTransfer> hederaTransfers = Optional.ofNullable(
                                    transaction.getAsJsonObject().get("transfers"))
                            .map(JsonElement::getAsJsonArray)
                            .orElseGet(JsonArray::new)
                            .asList()
                            .stream()
                            .map(transfer -> new HederaTransfer(
                                    transfer.getAsJsonObject().get("account").getAsString(),
                                    transfer.getAsJsonObject().get("amount").getAsLong()
                            ))
                            .toList();
                    final List<HederaTransfer> stakingRewardHederaTransfers = Optional.ofNullable(
                                    transaction.getAsJsonObject().get("staking_reward_transfers"))
                            .map(JsonElement::getAsJsonArray)
                            .orElseGet(JsonArray::new)
                            .asList()
                            .stream()
                            .map(transfer -> new HederaTransfer(
                                    transfer.getAsJsonObject().get("account").getAsString(),
                                    transfer.getAsJsonObject().get("amount").getAsLong()
                            ))
                            .toList();
                    return new HederaTransaction(
                            transaction.getAsJsonObject().get("transaction_id").getAsString(),
                            transaction.getAsJsonObject().get("consensus_timestamp").getAsString(),
                            hederaTransfers,
                            stakingRewardHederaTransfers
                    );
                })
                .toList();
    }
}
