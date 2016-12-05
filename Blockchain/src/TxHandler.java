import java.util.ArrayList;
import java.util.Arrays;


public class TxHandler {

	public class UTXOIndex {
		public byte[] txHash;
		public int index;
		
		public UTXOIndex (byte[] h, int i){
			h = Arrays.copyOf(h, h.length);
			index = i;
		}
		
		public boolean equals(UTXO u){
			return(Arrays.equals(u.getTxHash(), txHash) && index == u.getIndex());
		}
	}
	
	/*	UTXOPool for this class */
	UTXOPool pool;
	
	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		pool = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx's output values are non-negative, and
	 * (5) the sum of tx's input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		
		ArrayList<Transaction.Output> txOutputs = tx.getOutputs();
		ArrayList<Transaction.Input> txInputs = tx.getInputs();
		
		ArrayList<UTXO> allUTXOInPool = pool.getAllUTXO();
		ArrayList<UTXO> verifiedUTXOs = new ArrayList<UTXO>();
				
		double totalOutValue = 0;
		double totalInValue = 0;
		
		for(int x = 0; x < txOutputs.size(); ++x){
			if(txOutputs.get(x).value < 0){
				//negative output discovered
				return false;
			}
			totalOutValue += txOutputs.get(x).value;
		}
		
		for(int i = 0; i < txInputs.size(); ++i){
			Transaction.Input currentInput = txInputs.get(i);
			boolean inputVerified = false;
			
			for(int j = 0; j < allUTXOInPool.size(); ++j){
				UTXO currentUTXO = allUTXOInPool.get(j);
				
				if(Arrays.equals(currentInput.prevTxHash, currentUTXO.getTxHash()) &&
					currentInput.outputIndex == currentUTXO.getIndex()){
					//tx input's hash and index matched to a utxo
					//check if utxo is already claimed
					if(verifiedUTXOs.contains(currentUTXO)){
						//utxo already claimed
						return false;
					}
					else{
						//tx input verified
						inputVerified = true;
						verifiedUTXOs.add(currentUTXO);
						totalInValue += pool.getTxOutput(currentUTXO).value;
						//verify signature
						RSAKey key = pool.getTxOutput(currentUTXO).address;
						if(!(key.verifySignature(tx.getRawDataToSign(i), currentInput.signature))){
							return false;
						}
					}
				}
			}
			if(!inputVerified){
				return false;
			}
		}
		if(totalInValue < totalOutValue){
			return false;
		}

		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		ArrayList<Transaction> verifiedTxs = new ArrayList<Transaction>();
		for(Transaction t : possibleTxs){
			if(isValidTx(t)){
				verifiedTxs.add(t);
				
			}
		}
		Transaction[] ret = new Transaction[verifiedTxs.size()];
		for(int i = 0; i < verifiedTxs.size(); ++i){
			ret[i] = verifiedTxs.get(i);
		}

		return ret;
	}
	
	/* Returns the current UTXO pool.If no outstanding UTXOs, returns an empty (non-null) UTXOPool object. */
	public UTXOPool getUTXOPool(){
		return pool;
	}

} 