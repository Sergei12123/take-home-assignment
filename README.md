# Home Assignment: Scratch Game #
# Solution #


## Requirements ##

- JDK >= 1.8
- Maven

## How launch ##

```shell
mvn clean package
```
```shell
java -jar target/take-home-assignment-1.0.0-SNAPSHOT.jar --config config.json --betting-amount 1000
```

## Problems ##
1. It's not quite clear how the bonus symbols are generated. In my variant it is done so that only 1 bonus symbol is generated on the field and at the very beginning it is determined whether it will be present on the field or not.
2. It is not described what to do if several covered_areas of the same win_combination match for the same symbol. 
    
    For example, the symbol F may occur with quite a high probability and it may well be that the entire first column and the entire 2nd column consist only of symbols F? 
    
    In this case we do 

       bet_amount x reward(symbol_F) x reward(same_symbols_vertically)
   
    or else

       bet_amount x reward(symbol_F) x reward(same_symbols_vertically) x reward(same_symbols_vertically)
   
    In my solution I used the following variant
   > bet_amount x reward(symbol_F) x reward(same_symbols_vertically) x reward(same_symbols_vertically)
   
    Since it looks more logical

