package mandioca.bitcoin.rpc.response;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class GetWalletInfoResponse extends BitcoindRpcResponse {
    private String walletname;
    private int walletversion;
    private BigDecimal balance;
    private BigDecimal unconfirmedBalance;
    private BigDecimal immatureBalance;
    private long txcount;
    private long keypoololdest;
    private long keypoolsize;
    private long unlockedUntil;   // TODO Timestamp
    private BigDecimal paytxfee;
    private String hdseedid;
    private boolean privateKeysEnabled;

    public GetWalletInfoResponse() {
    }

    @Override
    public String toString() {
        return "GetWalletInfoResponse{" + "\n" +
                "  walletname='" + walletname + '\'' + "\n" +
                ", walletversion=" + walletversion + "\n" +
                ", balance=" + balance + "\n" +
                ", unconfirmedBalance=" + unconfirmedBalance + "\n" +
                ", immatureBalance=" + immatureBalance + "\n" +
                ", txcount=" + txcount + "\n" +
                ", keypoololdest=" + keypoololdest + "\n" +
                ", keypoolsize=" + keypoolsize + "\n" +
                ", unlockedUntil=" + unlockedUntil + "\n" +
                ", paytxfee=" + paytxfee + "\n" +
                ", hdseedid='" + hdseedid + '\'' + "\n" +
                ", privateKeysEnabled=" + privateKeysEnabled + "\n" +
                ", rpcErrorResponse=" + super.rpcErrorResponse + "\n" +
                '}';
    }
}

/*
$ ./bitcoin-cli help getwalletinfo
getwalletinfo
Returns an object containing various wallet state info.

Result:
{
  "walletname": xxxxx,               (string) the wallet name
  "walletversion": xxxxx,            (numeric) the wallet version
  "balance": xxxxxxx,                (numeric) the total confirmed balance of the wallet in BTC
  "unconfirmed_balance": xxx,        (numeric) the total unconfirmed balance of the wallet in BTC
  "immature_balance": xxxxxx,        (numeric) the total immature balance of the wallet in BTC
  "txcount": xxxxxxx,                (numeric) the total number of transactions in the wallet
  "keypoololdest": xxxxxx,           (numeric) the timestamp (seconds since Unix epoch) of the oldest pre-generated key in the key pool
  "keypoolsize": xxxx,               (numeric) how many new keys are pre-generated (only counts external keys)
  "keypoolsize_hd_internal": xxxx,   (numeric) how many new keys are pre-generated for internal use (used for change outputs, only appears if the wallet is using this feature, otherwise external keys are used)
  "unlocked_until": ttt,             (numeric) the timestamp in seconds since epoch (midnight Jan 1 1970 GMT) that the wallet is unlocked for transfers, or 0 if the wallet is locked
  "paytxfee": x.xxxx,                (numeric) the transaction fee configuration, set in BTC/kB
  "hdseedid": "<hash160>"            (string, optional) the Hash160 of the HD seed (only present when HD is enabled)
  "private_keys_enabled": true|false (boolean) false if privatekeys are disabled for this wallet (enforced watch-only wallet)
}

Examples:
> bitcoin-cli getwalletinfo
> curl --user myusername --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getwalletinfo", "params": [] }' -H 'content-type: text/plain;' http://127.0.0.1:8332/
 */
