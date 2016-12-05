import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {
   public static final int CUT_OFF_AGE = 10;

   // all information required in handling a block in block chain
   private class BlockNode {
      public Block b;
      public BlockNode parent;
      public ArrayList<BlockNode> children; // what is the point of a LIST of children?
      // shouldn't there be only one child per node?
      public int height;
      // utxo pool for making a new block on top of this block
      private UTXOPool uPool;

      public BlockNode(Block b, BlockNode parent, UTXOPool uPool) {
         this.b = b;
         this.parent = parent;
         children = new ArrayList<BlockNode>();
         this.uPool = uPool;
         if (parent != null) {
            height = parent.height + 1;
            parent.children.add(this);
         } else {
            height = 1;
         }
      }

      public UTXOPool getUTXOPoolCopy() {
         return new UTXOPool(uPool);
      }
   }
   
   // List of nodes
   private ArrayList<BlockNode> chain;
   //UTXOPool pending = new UTXOPool();
   private TransactionPool pool;
   private int height;
   private BlockNode maxHeightBlock;
   //hashmap?
   private HashMap<ByteArrayWrapper, BlockNode> H;

   /* create an empty block chain with just a genesis block.
    * Assume genesis block is a valid block
    */
   public BlockChain(Block genesisBlock) {
      // IMPLEMENT THIS
	   //chain.add(new BlockNode(genesisBlock,null,pending));
	   // create a genesis block to start the chain
	   // need the gblock, parent(none), and a pool.
	   // create the pool by extracting the gblock's coinbase
	   // aka the genesis transaction
	   // and it becomes the output.
	   UTXOPool uPool = new UTXOPool();
	   Transaction coinbase = genesisBlock.getCoinbase();
	   UTXO uCoinBase = new UTXO(coinbase.getHash(),0);
	   uPool.addUTXO(uCoinBase, coinbase.getOutput(0));
	   BlockNode genesis = new BlockNode(genesisBlock, null, uPool);
	   
	   //set up the chain
	   chain = new ArrayList<BlockNode>();
	   chain.add(genesis);
	   maxHeightBlock = genesis;
	   height = 1;
	   
	   pool = new TransactionPool();
	   
	   //hashmap?
	   H = new HashMap<ByteArrayWrapper, BlockNode>();
	   H.put(new ByteArrayWrapper(genesisBlock.getHash()), genesis);
	   
	   //return;
   }

   /* Get the maximum height block
    */
   public Block getMaxHeightBlock() {
      // IMPLEMENT THIS
	   return maxHeightBlock.b;
	   //return chain.get(height-1).b;
	   //return null;
   }
   
   /* Get the UTXOPool for mining a new block on top of 
    * max height block
    */
   public UTXOPool getMaxHeightUTXOPool() {
      // IMPLEMENT THIS
	   return maxHeightBlock.uPool;
//	   return chain.get(height-1).getUTXOPoolCopy();
	   //return null;
   }
   
   /* Get the transaction pool to mine a new block
    */
   public TransactionPool getTransactionPool() {
      // IMPLEMENT THIS
	   return pool;
	   //return null;
   }

   /* Add a block to block chain if it is valid.
    * For validity, all transactions should be valid
    * and block should be at height > (maxHeight - CUT_OFF_AGE).
    * For example, you can try creating a new block over genesis block 
    * (block height 2) if blockChain height is <= CUT_OFF_AGE + 1. 
    * As soon as height > CUT_OFF_AGE + 1, you cannot create a new block at height 2.
    * Return true of block is successfully added
    */
   public boolean addBlock(Block b) {
       // IMPLEMENT THIS
	   
	   //ByteArrayWrapper prevhashW = new ByteArrayWrapper(b.getPrevBlockHash());
	   byte[] PrevBlockHash = b.getPrevBlockHash();
	   if (PrevBlockHash == null) return false; // fix stupid nullpointerexception
	   
	   ByteArrayWrapper prevhashW = new ByteArrayWrapper(PrevBlockHash);
	   
	   if(b.getPrevBlockHash() == null || b.equals(null)) return false;
	   //BlockNode newBlock = new BlockNode(b,this.height,pending);
	   
	   //hash?
	   // the new block had better point to an existing block's hash
	   if (!this.H.containsKey(prevhashW)) return false;
	   
	   //validate transactions
	   
	   		//create copy of transaction list, just in case
	   ArrayList<Transaction> txs = b.getTransactions();
	   Transaction[] txArray = new Transaction[txs.size()];
	   txArray = txs.toArray(txArray);
	   Transaction[] txArrayCopy = Arrays.copyOf(txArray, txArray.length);
	   		//compare the block's transactions with our pool's transactions
	   TxHandler txHandler = new TxHandler(this.H.get(prevhashW).uPool);
	   if (!Arrays.equals(txArrayCopy, txHandler.handleTxs(txArray))) {
		   return false;
	   }
	   
	   //almost done, start creating new BlockNode
	   
	   Transaction coinbase = b.getCoinbase();
	   UTXO uCoinBase = new UTXO(coinbase.getHash(),0);
	   UTXOPool uPool = new UTXOPool(txHandler.getUTXOPool());
	   uPool.addUTXO(uCoinBase, coinbase.getOutput(0));
	   BlockNode newBlock = new BlockNode(b, this.H.get(prevhashW), uPool);
	   
	   //final check: block height
	   if (newBlock.parent.height < (this.height - BlockChain.CUT_OFF_AGE)) return false;
	   
	   //update height
	   if (newBlock.height > this.height) this.height = newBlock.height;
	   
	   //all checks passed, start adding block
	   this.maxHeightBlock.children.add(newBlock);
	   this.maxHeightBlock = newBlock;
	   this.H.put(new ByteArrayWrapper(newBlock.b.getHash()), newBlock);
	   
	   // extract the block's transactions
	   for(Transaction t: b.getTransactions()){
		   this.addTransaction(t);
	   }
	   
	   return false;
   }

   /* Add a transaction in transaction pool
    */
   public void addTransaction(Transaction tx) {
      // IMPLEMENT THIS
	   pool.addTransaction(tx);
   }
}