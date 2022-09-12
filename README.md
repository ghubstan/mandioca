# Mandioca

A Java project for helping me learn a bit about how Elliptic Curve Math and Bitcoin work.

It contains Java ports of some [Python examples](https://github.com/jimmysong/programmingbitcoin) contained in
Jimmy Song's Book [Programming Bitcoin](https://www.oreilly.com/library/view/programming-bitcoin/9781492031482).

## Caveats

- This was not a project concerned with proper Java OOP.
- Bitcoin-Core version 0.18.1 was used while I worked on it in 2019.
- The code & tests in the `mandioca.bitcoin.network`, `mandioca.bitcoin.rpc`, and `mandioca.bitcoin.transaction`
  packages depend on a running testnet Bitcoin-Core vv0.18.1 daemon, and I have not polished any of my private setup
  scripts for this public repo.
- All docs and scripts specific to my development env are excluded from this public repo.

