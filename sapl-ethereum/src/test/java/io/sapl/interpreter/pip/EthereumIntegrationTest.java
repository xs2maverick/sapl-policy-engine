package io.sapl.interpreter.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.sapl.api.pip.AttributeException;
import io.sapl.interpreter.pip.contracts.Authorization;

public class EthereumIntegrationTest {

    private static final String KEYSTORE = "ethereum-testnet/ptn/keystore/";

    private static final String USER1WALLET = "UTC--2019-05-10T11-32-05.64000000Z--70b6613e37616045a80a97e08e930e1e4d800039.json";
    private static final String USER2WALLET = "UTC--2019-05-10T11-32-55.438000000Z--3f2cbea2185089ea5bbabbcd7616b215b724885c.json";
    private static final String USER3WALLET = "UTC--2019-05-10T11-33-01.363000000Z--2978263a3ecacb01c75e51e3f74b37016ee3904c.json";
    private static final String USER4WALLET = "UTC--2019-05-10T11-33-10.665000000Z--23a28c4cbad79cf61c8ad2e47d5134b06ef0bb73.json";
    private static final String TEST_VALUE = "testValue";

    private static Web3j web3j;
    private static EthereumPolicyInformationPoint ethPip;
    private static final JsonNodeFactory factory = new JsonNodeFactory(true);
    private static final Logger logger = LoggerFactory.getLogger(EthereumIntegrationTest.class);

    private static String user1Address;
    private static String user2Address;
    private static String user3Address;
    private static String user4Address;

    private static String authContractAddress;

    private static TransactionReceipt transactionReceiptUser2;
    private static TransactionReceipt transactionReceiptUser3;
    private static TransactionReceipt transactionReceiptUser4;

    // TEST INFORMATION: Before launching the test please start the local Ethereum
    // private testnet via one of the startChain scripts
    // inside the folder ethereum-testnet.
    // For the scripts to work properly please follow these steps:
    // 1. Download and install Geth (https://geth.ethereum.org/downloads/) (This has
    // been tested with version 1.9.3).
    // 2. Navigate to the ethereum-testnet folder inside the project in a terminal
    // or the PowerShell.
    // 3. Execute the startChain.ps1 script in Windows or the startChain script in
    // Linux to initialize and start a private, local version of the Ethereum
    // blockchain.
    // 4. Run the test.
    // 5. After the test has finished, type exit in the Geth console to stop the
    // blockchain. If you used the script, the leftovers of the blockchain should be
    // automatically deleted.

    @BeforeClass
    public static void init() throws InterruptedException, TransactionException, Exception {
	web3j = Web3j.build(new HttpService());
	ethPip = new EthereumPolicyInformationPoint(new HttpService());

	// TODO Automatically start a local Ethereum private testnet

	// In this first section we load the accounts from the blockchain
	List<String> accounts;

	accounts = web3j.ethAccounts().send().getAccounts();
	user1Address = accounts.get(0);
	user2Address = accounts.get(1);
	user3Address = accounts.get(2);
	user4Address = accounts.get(3);

	// Now we make some transactions
	Credentials credentials = WalletUtils.loadCredentials("", KEYSTORE + USER1WALLET);
	transactionReceiptUser2 = Transfer
		.sendFunds(web3j, credentials, user2Address, BigDecimal.valueOf(2.0), Convert.Unit.ETHER).send();
	transactionReceiptUser3 = Transfer
		.sendFunds(web3j, credentials, user3Address, BigDecimal.valueOf(3.3), Convert.Unit.ETHER).send();
	transactionReceiptUser4 = Transfer
		.sendFunds(web3j, credentials, user4Address, BigDecimal.valueOf(4.444), Convert.Unit.ETHER).send();

	// Now we deploy a contract and make some changes
	Authorization authContract = Authorization.deploy(web3j, credentials, new DefaultGasProvider()).send();
	authContractAddress = authContract.getContractAddress();
	authContract.authorize(user2Address).send();

    }

    // verifyTransaction

    @Test
    public void verifyTransactionShouldReturnTrueWithCorrectTransaction() {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("transactionHash", transactionReceiptUser2.getTransactionHash());
	saplObject.put("fromAccount", user1Address);
	saplObject.put("toAccount", user2Address);
	saplObject.put("transactionValue", new BigInteger("2000000000000000000"));
	boolean result = ethPip.verifyTransaction(saplObject, null).blockFirst().asBoolean();
	assertTrue("Transaction was not validated as true although it is correct.", result);

    }

    @Test
    public void verifyTransactionShouldReturnFalseWithFalseValue() {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("transactionHash", transactionReceiptUser2.getTransactionHash());
	saplObject.put("fromAccount", user1Address);
	saplObject.put("toAccount", user2Address);
	saplObject.put("transactionValue", new BigInteger("25"));
	boolean result = ethPip.verifyTransaction(saplObject, null).blockFirst().asBoolean();
	assertFalse("Transaction was not validated as false although the value was false.", result);

    }

    @Test
    public void verifyTransactionShouldReturnFalseWithFalseSender() {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("transactionHash", transactionReceiptUser2.getTransactionHash());
	saplObject.put("fromAccount", user3Address);
	saplObject.put("toAccount", user2Address);
	saplObject.put("transactionValue", new BigInteger("2000000000000000000"));
	boolean result = ethPip.verifyTransaction(saplObject, null).blockFirst().asBoolean();
	assertFalse("Transaction was not validated as false although the sender was false.", result);

    }

    @Test
    public void verifyTransactionShouldReturnFalseWithFalseRecipient() {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("transactionHash", transactionReceiptUser2.getTransactionHash());
	saplObject.put("fromAccount", user1Address);
	saplObject.put("toAccount", user3Address);
	saplObject.put("transactionValue", new BigInteger("2000000000000000000"));
	boolean result = ethPip.verifyTransaction(saplObject, null).blockFirst().asBoolean();
	assertFalse("Transaction was not validated as false although the recipient was false.", result);

    }

    @Test
    public void verifyTransactionShouldReturnFalseWithFalseTransactionHash() {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("transactionHash", transactionReceiptUser3.getTransactionHash());
	saplObject.put("fromAccount", user1Address);
	saplObject.put("toAccount", user2Address);
	saplObject.put("transactionValue", new BigInteger("2000000000000000000"));
	boolean result = ethPip.verifyTransaction(saplObject, null).blockFirst().asBoolean();
	assertFalse("Transaction was not validated as false although the TransactionHash was false.", result);

    }

    @Test
    public void verifyTransactionShouldReturnFalseWithNullInput() {
	boolean result = ethPip.verifyTransaction(null, null).blockFirst().asBoolean();
	assertFalse("Transaction was not validated as false although the input was null.", result);

    }

    @Test
    public void verifyTransactionShouldReturnFalseWithWrongInput() {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("wrongName", transactionReceiptUser2.getTransactionHash());
	saplObject.put("fromAccount", user1Address);
	saplObject.put("toAccount", user2Address);
	saplObject.put("transactionValue", new BigInteger("2000000000000000000"));
	boolean result = ethPip.verifyTransaction(saplObject, null).blockFirst().asBoolean();
	assertFalse("Transaction was not validated as false although the input was erroneous.", result);

    }

    // loadContractInformation
    @Test
    @Ignore
    public void loadContractInformationShouldReturnCorrectValue() throws AttributeException {
	ObjectNode saplObject = factory.objectNode();
	saplObject.put("fromAccount", user1Address);
	saplObject.put("toAccount", authContractAddress);
	saplObject.put("functionName", "isAuthorized");
	ArrayNode inputParams = factory.arrayNode();
	ObjectNode input1 = factory.objectNode();
	input1.put("type", "address");
	input1.put("value", user2Address.substring(2));
	inputParams.add(input1);
	saplObject.set("inputParams", inputParams);
	ArrayNode outputParams = factory.arrayNode();
	outputParams.add("bool");
	saplObject.set("outputParams", outputParams);
	boolean result = ethPip.loadContractInformation(saplObject, null).blockFirst().asBoolean();
	assertTrue("False was returned although user2 was authorized and result should have been true.", result);

    }

    // web3_clientVersion

    @Test
    public void web3ClientVersionShouldReturnTheClientVersion() throws IOException {
	String pipClientVersion = ethPip.web3ClientVersion(null, null).blockFirst().asText();
	String web3jClientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
	assertEquals("The web3ClientVersion from the PIP was not loaded correctly.", pipClientVersion,
		web3jClientVersion);
    }

    // web3_sha3
    @Test
    public void web3Sha3ShouldReturnCorrectValuer() throws IOException {
	JsonNode saplObject = factory.textNode(TEST_VALUE);
	String pipResult = ethPip.web3Sha3(saplObject, null).blockFirst().textValue();
	String web3jResult = web3j.web3Sha3(TEST_VALUE).send().getResult();
	assertEquals("The web3Sha3 method did not work correctly.", pipResult, web3jResult);
    }

    // net_version
    @Test
    public void netVersionShouldReturnCorrectValue() throws IOException {
	String pipResult = ethPip.netVersion(null, null).blockFirst().textValue();
	String web3jResult = web3j.netVersion().send().getNetVersion();
	assertEquals("The netVersion method did not work correctly.", pipResult, web3jResult);

    }

    // net_listening
    @Test
    public void netListeningShouldReturnTrueWhenListeningToNetworkConnections() throws IOException {
	assertTrue("The netListening method did not return true although the Client by default is listening.",
		ethPip.netListening(null, null).blockFirst().asBoolean());
    }

    // net_peerCount
    @Test
    public void netPeerCountShouldReturnTheCorrectNumber() throws IOException {
	BigInteger pipResult = ethPip.netPeerCount(null, null).blockFirst().bigIntegerValue();
	BigInteger web3jResult = web3j.netPeerCount().send().getQuantity();
	assertEquals("The NetPeerCount method did not return the correct number.", pipResult, web3jResult);
    }

}
