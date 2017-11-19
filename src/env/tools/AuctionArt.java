package tools;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

// import needed libraries for java and web3j
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Convert;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Utf8String;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.tx.Contract;

import tools.AuctionMAS;

/**
 *      Artifact that implements the auction.
 */
public class AuctionArt extends Artifact {

    private File walletfile = new File("/home/fernandopapi/.local/share/io.parity.ethereum/keys/kovan/UTC--2017-10-27T13-00-23Z--8a39cd59-fd1e-4224-5aed-d0444c7b3072");
    private BigInteger gasLimit = BigInteger.valueOf(40000000000L);
    private BigInteger gasPrice = BigInteger.valueOf(4_300_000);
    private String password = "password";
    private String auctionId = new String();

    private Web3j web3 = Web3j.build(new HttpService());        // defaults to http://localhost:8545/
    private Parity parity = Parity.build(new HttpService());    // defaults to http://localhost:8545/

    @OPERATION public void init(String taskDs, int maxValue)  {
        // observable properties
        try{
            Credentials credentials = WalletUtils.loadCredentials(password, walletfile);
            AuctionMAS auctionContract = AuctionMAS.load("0x040185003FE21AFD615De97330Dc61C2C8707f19", web3, credentials, gasPrice, gasLimit);

            System.out.println("Creating auction for " + taskDs + "...");
            TransactionReceipt transactionReceipt = auctionContract.CreateAuction(new Utf8String(taskDs),
                                                                                  new Uint256(maxValue),
                                                                                  new Uint256(maxValue),
                                                                                  new Utf8String("no_winner")).get();

            System.out.println("Done!");
            defineObsProperty("task", taskDs);
            defineObsProperty("maxValue", maxValue);
            defineObsProperty("currentBid", maxValue);
            defineObsProperty("currentWinner", "no_winner");
            auctionId = taskDs;
            defineObsProperty("auctionId", taskDs);

        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    @OPERATION public void bid(double bidValue) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletfile);
            AuctionMAS auctionContract = AuctionMAS.load("0x040185003FE21AFD615De97330Dc61C2C8707f19", web3, credentials, gasPrice, gasLimit);
            ObsProperty opAuctionId = getObsProperty("auctionId");
            // System.out.println("opAuctionId " + auctionId);
            String agentBidding = getCurrentOpAgentId().getAgentName();

            BigInteger bidValueInteger = new BigDecimal(bidValue).toBigInteger();
            System.out.println(agentBidding + " will bid " + bidValueInteger +  " for " + auctionId + " ...");

            TransactionReceipt transactionReceipt = auctionContract.placeBid(new Utf8String(auctionId),
                                                                             new Uint256(bidValueInteger),
                                                                             new Utf8String(agentBidding)).get();
            // System.out.println("Done!");

            ObsProperty opCurrentValue = getObsProperty("currentBid");
            ObsProperty opCurrentWinner = getObsProperty("currentWinner");

            String currentWinner;
            currentWinner = auctionContract.getCurrentWinnerbyAuctionID(new Utf8String(auctionId)).get().getValue();
            // System.out.println("Updating Winner: " + currentWinner);
            opCurrentWinner.updateValue(currentWinner);

            BigInteger currentBid;
            currentBid = auctionContract.getCurrentBidbyAuctionID(new Utf8String(auctionId)).get().getValue();
            // System.out.println("Updating Value: " + currentBid);
            opCurrentValue.updateValue(currentBid);

            // System.out.println(agentBidding + " bid for " + auctionId + " and winner is " + currentWinner + " with bid " + currentBid);

            // if (bidValue < opCurrentValue.doubleValue()) {  // the bid is better than the previous
            //     opCurrentValue.updateValue(bidValue);
            //     opCurrentWinner.updateValue(getCurrentOpAgentId().getAgentName());
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
