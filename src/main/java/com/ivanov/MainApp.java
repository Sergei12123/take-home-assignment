package com.ivanov;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ivanov.configDtos.Combination;
import com.ivanov.configDtos.GameConfig;
import com.ivanov.configDtos.enums.Type;
import com.ivanov.configDtos.enums.When;
import com.ivanov.dtos.Result;
import com.ivanov.dtos.WinCombinations;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.ivanov.Constants.*;
import static com.ivanov.configDtos.enums.Impact.EXTRA_BONUS;
import static com.ivanov.configDtos.enums.Impact.MULTIPLY_REWARD;

public class MainApp {

    public static int bonusRow = 0;
    public static int bonusColumn = 0;

    public static boolean isBonusExist = false;

    public static Map<String, List<List<String>>> linearCombinations = new HashMap<>();
    public static List<String> standardSymbols = new ArrayList<>();

    public static void main(String[] args) {
        GameConfig gameConfig = loadGameConfig(args);
        BigDecimal bettingAmount = loadBettingAmount(args);
        WinCombinations winCombinations = initWinCombinations(gameConfig);

        var field = getFieldAndFillWinCombinations(winCombinations, gameConfig);
        BigDecimal res = calculateResultAndFillAppliedWinningCombinations(bettingAmount, field, gameConfig, winCombinations);

        outputResult(field, res, winCombinations.getAppliedWinningCombinations());

    }

    private static BigDecimal calculateResultAndFillAppliedWinningCombinations(BigDecimal bettingAmount, String[][] field, GameConfig gameConfig, WinCombinations winCombinations) {
        BigDecimal resMultiply = BigDecimal.ZERO;
        AtomicReference<BigDecimal> res = new AtomicReference<>(BigDecimal.ZERO);
        for (var symbol : standardSymbols) {
            Map.Entry<String, List<String>> symbolMultipliers = getSymbolMultipliers(symbol, winCombinations, gameConfig);
            if (!symbolMultipliers.getValue().isEmpty()) {
                winCombinations.getAppliedWinningCombinations().put(symbolMultipliers.getKey(), symbolMultipliers.getValue());
                resMultiply = resMultiply.add(calculateForSymbol(symbolMultipliers, gameConfig));
            }
        }
        if (!resMultiply.equals(BigDecimal.ZERO)) {
            res.set(bettingAmount.multiply(resMultiply));
            if (isBonusExist) {
                gameConfig.getSymbols().entrySet().stream()
                        .filter(symbol -> symbol.getKey().equals(field[bonusRow][bonusColumn]))
                        .map(Map.Entry::getValue)
                        .forEach(symbol -> {
                            if (symbol.getImpact().equals(EXTRA_BONUS)) {
                                res.updateAndGet(v -> v.add(symbol.getExtra()));
                            } else if (symbol.getImpact().equals(MULTIPLY_REWARD)) {
                                res.updateAndGet(v -> v.multiply(symbol.getRewardMultiplier()));
                            }
                        });
            }

        }
        return res.get();
    }

    private static String[][] getFieldAndFillWinCombinations(WinCombinations winCombinations, GameConfig gameConfig) {
        String[][] field = new String[gameConfig.getRows()][gameConfig.getColumns()];
        isBonusExist = new Random().nextBoolean();
        if (isBonusExist) {
            bonusColumn = new Random().nextInt(gameConfig.getColumns());
            bonusRow = new Random().nextInt(gameConfig.getRows());
        }
        for (int row = 0; row < gameConfig.getRows(); row++) {
            for (int column = 0; column < gameConfig.getColumns(); column++) {
                String symbol = getSymbol(row, column, gameConfig);
                field[row][column] = symbol;
                if (standardSymbols.contains(symbol)) {
                    winCombinations.getCountOfEachSymbol().put(symbol, winCombinations.getCountOfEachSymbol().get(symbol) + 1);
                    int finalRow = row;
                    int finalColumn = column;
                    linearCombinations.entrySet().stream()
                            .filter(combination -> combination.getValue().stream().anyMatch(el -> el.contains(String.format(AREA_FORMAT, finalRow, finalColumn))))
                            .forEach(combination -> winCombinations.getCombinations().get(combination.getKey())
                                    .stream()
                                    .filter(el -> el.containsKey(String.format(AREA_FORMAT, finalRow, finalColumn)))
                                    .forEach(el -> el.put(String.format(AREA_FORMAT, finalRow, finalColumn), symbol))
                            );
                }
            }
        }
        return field;
    }


    private static WinCombinations initWinCombinations(GameConfig gameConfig) {
        linearCombinations = gameConfig.getWinCombinations().entrySet().stream()
                .filter(combination -> combination.getValue().getWhen().equals(When.LINEAR_SYMBOLS))
                .collect(Collectors.toMap(Map.Entry::getKey, stringCombinationEntry -> stringCombinationEntry.getValue().getCoveredAreas()));
        standardSymbols = gameConfig.getSymbols().entrySet().stream()
                .filter(symbol -> symbol.getValue().getType().equals(Type.STANDARD))
                .map(Map.Entry::getKey)
                .toList();
        return new WinCombinations(linearCombinations, standardSymbols);
    }

    private static GameConfig loadGameConfig(String[] args) {
        String configFilePath = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(CONFIG_PARAM)) {
                if (i + 1 < args.length) {
                    configFilePath = args[++i];
                } else {
                    throw new IllegalArgumentException("Expected value after " + CONFIG_PARAM);
                }
                break;
            }
        }
        if (configFilePath == null) {
            throw new IllegalArgumentException(CONFIG_PARAM + "param is required");
        }
        try {
            return new ObjectMapper().readValue(new File(configFilePath), GameConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't load game config");
        }
    }

    private static BigDecimal loadBettingAmount(String[] args) {
        BigDecimal bettingAmount = BigDecimal.ZERO;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(BETTING_AMOUNT_PARAM)) {
                if (i + 1 < args.length) {
                    bettingAmount = BigDecimal.valueOf(Double.parseDouble(args[++i]));
                } else {
                    throw new IllegalArgumentException("Expected value after " + BETTING_AMOUNT_PARAM);
                }
                break;
            }
        }
        if (bettingAmount.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException(BETTING_AMOUNT_PARAM + " must be greater than 0");
        }

        return bettingAmount;
    }

    private static void outputResult(String[][] field, BigDecimal res, Map<String, List<String>> appliedWinningCombinations) {
        Result result = new Result();
        result.setMatrix(field);
        result.setReward(res.stripTrailingZeros().scale() < 0 ? res.setScale(0, RoundingMode.FLOOR) : res.stripTrailingZeros());
        if (!result.getReward().equals(BigDecimal.ZERO)) {
            result.setAppliedWinningCombinations(appliedWinningCombinations);
            if (isBonusExist) result.setAppliedBonusSymbol(field[bonusRow][bonusColumn]);
        }
        var objectMapper2 = new ObjectMapper();
        objectMapper2.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            var resultString = objectMapper2.writeValueAsString(result)
                    .replaceAll("], \\[", "],\n\t[")
                    .replaceAll("\\[ \\[", "[\n\t[")
                    .replaceAll("] ]", "]\r\n  ]");
            System.out.println(resultString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getSymbol(int row, int column, GameConfig gameConfig) {
        return getRandomSymbol(isBonusExist && row == bonusRow && column == bonusColumn ?
                gameConfig.getProbabilities().getBonusSymbols().getSymbols() :
                gameConfig.getProbabilities().getStandardSymbols().stream()
                        .filter(s -> s.getColumn() == column && s.getRow() == row)
                        .findFirst()
                        .orElse(gameConfig.getProbabilities().getStandardSymbols().stream()
                                .filter(s -> s.getColumn() == 0 && s.getRow() == 0)
                                .findFirst()
                                .orElseThrow())
                        .getSymbols()
        );
    }

    private static String getRandomSymbol(Map<String, Integer> symbols) {
        int totalWeight = symbols.values().stream().mapToInt(Integer::intValue).sum();
        int randomWeight = new Random().nextInt(totalWeight);

        int cumulativeWeight = 0;
        String heaviestSymbol = null;
        int heaviestWeight = 0;
        for (var entry : symbols.entrySet()) {
            if (entry.getValue() > heaviestWeight) {
                heaviestWeight = entry.getValue();
                heaviestSymbol = entry.getKey();
            }
            cumulativeWeight += entry.getValue();
            if (randomWeight < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return heaviestSymbol;
    }

    private static Map.Entry<String, List<String>> getSymbolMultipliers(String symbol, WinCombinations winCombinations, GameConfig gameConfig) {
        Map.Entry<String, List<String>> result = new AbstractMap.SimpleEntry<>(symbol, new ArrayList<>());
        Integer countOfElement = winCombinations.getCountOfEachSymbol().get(symbol);
        gameConfig.getWinCombinations().entrySet().stream()
                .filter(el -> el.getValue().getWhen().equals(When.SAME_SYMBOLS) && el.getValue().getCount() == countOfElement)
                .findFirst()
                .ifPresent(el -> result.getValue().add(el.getKey()));

        winCombinations.getCombinations().entrySet().stream()
                .flatMap(el -> el.getValue().stream().map(el2 -> Map.entry(el.getKey(), el2)))
                .filter(el -> el.getValue().values().stream().allMatch(el2 -> el2.equals(symbol)))
                .map(Map.Entry::getKey)
                .forEach(el -> result.getValue().add(el));

        return result;
    }

    private static BigDecimal calculateForSymbol(Map.Entry<String, List<String>> symbolMultipliers, GameConfig gameConfig) {
        return gameConfig.getSymbols().get(symbolMultipliers.getKey()).getRewardMultiplier().multiply(
                gameConfig.getWinCombinations().entrySet().stream()
                        .filter(el -> symbolMultipliers.getValue().contains(el.getKey()))
                        .map(Map.Entry::getValue)
                        .map(Combination::getRewardMultiplier)
                        .reduce(BigDecimal::multiply)
                        .orElse(BigDecimal.ONE));
    }

}


