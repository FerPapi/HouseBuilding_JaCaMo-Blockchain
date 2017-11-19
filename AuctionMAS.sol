pragma solidity ^0.4.18;

contract AuctionMAS {

    struct Auction {
        string task;
        uint256 maxValue;
        uint256 currentBid;
        string currentWinner; //use addresses for each agent in future
    }


    mapping (string => Auction) auctionList; // Works like a non iterable dictionary in python
    uint public auctionCount = 0;


    function AuctionMAS() public {
    }
    function getAuctionCount() view public returns (uint) {
        return auctionCount;
    }
    
    function CreateAuction( string _task,
                            uint256 _maxValue,
                            uint256 _currentBid,
                            string _currentWinner) public returns (uint auctionId) {

        auctionCount++;
        auctionList[_task].task = _task;
        auctionList[_task].maxValue = _maxValue;
        auctionList[_task].currentBid = _currentBid;
        auctionList[_task].currentWinner = _currentWinner;

        return auctionId;
    }

    function placeBid(string task, uint bidValue, string bidder) public {
        Auction storage a = auctionList[task];
        if (a.currentBid > bidValue){
            a.currentBid = bidValue ;
            a.currentWinner = bidder;
        }

    }

    function getCurrentWinnerbyAuctionID(string task) public view returns (string winner) {
        Auction storage a = auctionList[task];
        return a.currentWinner;
    }

    function getCurrentBidbyAuctionID(string task) public view returns (uint curretBid) {
        Auction storage a = auctionList[task];
        return a.currentBid;
    }

}
