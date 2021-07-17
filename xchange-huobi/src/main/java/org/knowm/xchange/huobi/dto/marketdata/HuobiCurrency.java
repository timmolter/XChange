package org.knowm.xchange.huobi.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HuobiCurrency {

    private String chain;
    private String displayName;
    private String baseChain;
    private String baseChainProtocol;
    private boolean isDynamic;
    private String depositStatus;
    private String maxTransactFeeWithdraw;
    private String maxWithdrawAmt;
    private String minDepositAmt;
    private String minTransactFeeWithdraw;
    private String minWithdrawAmt;
    private int numOfConfirmations;
    private int numOfFastConfirmations;
    private String transactFeeWithdraw;
    private String withdrawFeeType;
    private int withdrawPrecision;
    private String withdrawQuotaPerDay;
    private String withdrawQuotaPerYear;
    private String withdrawQuotaTotal;
    private String withdrawStatus;

    public HuobiCurrency(@JsonProperty("chain") String chain,
                         @JsonProperty("displayName") String displayName,
                         @JsonProperty("baseChain") String baseChain,
                         @JsonProperty("baseChainProtocol") String baseChainProtocol,
                         @JsonProperty("isDynamic") boolean isDynamic,
                         @JsonProperty("depositStatus") String depositStatus,
                         @JsonProperty("maxTransactFeeWithdraw") String maxTransactFeeWithdraw,
                         @JsonProperty("maxWithdrawAmt") String maxWithdrawAmt,
                         @JsonProperty("minDepositAmt") String minDepositAmt,
                         @JsonProperty("minTransactFeeWithdraw") String minTransactFeeWithdraw,
                         @JsonProperty("minWithdrawAmt") String minWithdrawAmt,
                         @JsonProperty("numOfConfirmations") int numOfConfirmations,
                         @JsonProperty("numOfFastConfirmations") int numOfFastConfirmations,
                         @JsonProperty("transactFeeWithdraw") String transactFeeWithdraw,
                         @JsonProperty("withdrawFeeType") String withdrawFeeType,
                         @JsonProperty("withdrawPrecision") int withdrawPrecision,
                         @JsonProperty("withdrawQuotaPerDay") String withdrawQuotaPerDay,
                         @JsonProperty("withdrawQuotaPerYear") String withdrawQuotaPerYear,
                         @JsonProperty("withdrawQuotaTotal") String withdrawQuotaTotal,
                         @JsonProperty("withdrawStatus") String withdrawStatus) {
        this.chain = chain;
        this.displayName = displayName;
        this.baseChain = baseChain;
        this.baseChainProtocol = baseChainProtocol;
        this.isDynamic = isDynamic;
        this.depositStatus = depositStatus;
        this.maxTransactFeeWithdraw = maxTransactFeeWithdraw;
        this.maxWithdrawAmt = maxWithdrawAmt;
        this.minDepositAmt = minDepositAmt;
        this.minTransactFeeWithdraw = minTransactFeeWithdraw;
        this.minWithdrawAmt = minWithdrawAmt;
        this.numOfConfirmations = numOfConfirmations;
        this.numOfFastConfirmations = numOfFastConfirmations;
        this.transactFeeWithdraw = transactFeeWithdraw;
        this.withdrawFeeType = withdrawFeeType;
        this.withdrawPrecision = withdrawPrecision;
        this.withdrawQuotaPerDay = withdrawQuotaPerDay;
        this.withdrawQuotaPerYear = withdrawQuotaPerYear;
        this.withdrawQuotaTotal = withdrawQuotaTotal;
        this.withdrawStatus = withdrawStatus;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBaseChain() {
        return baseChain;
    }

    public void setBaseChain(String baseChain) {
        this.baseChain = baseChain;
    }

    public String getBaseChainProtocol() {
        return baseChainProtocol;
    }

    public void setBaseChainProtocol(String baseChainProtocol) {
        this.baseChainProtocol = baseChainProtocol;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    public String getDepositStatus() {
        return depositStatus;
    }

    public void setDepositStatus(String depositStatus) {
        this.depositStatus = depositStatus;
    }

    public String getMaxTransactFeeWithdraw() {
        return maxTransactFeeWithdraw;
    }

    public void setMaxTransactFeeWithdraw(String maxTransactFeeWithdraw) {
        this.maxTransactFeeWithdraw = maxTransactFeeWithdraw;
    }

    public String getMaxWithdrawAmt() {
        return maxWithdrawAmt;
    }

    public void setMaxWithdrawAmt(String maxWithdrawAmt) {
        this.maxWithdrawAmt = maxWithdrawAmt;
    }

    public String getMinDepositAmt() {
        return minDepositAmt;
    }

    public void setMinDepositAmt(String minDepositAmt) {
        this.minDepositAmt = minDepositAmt;
    }

    public String getMinTransactFeeWithdraw() {
        return minTransactFeeWithdraw;
    }

    public void setMinTransactFeeWithdraw(String minTransactFeeWithdraw) {
        this.minTransactFeeWithdraw = minTransactFeeWithdraw;
    }

    public String getMinWithdrawAmt() {
        return minWithdrawAmt;
    }

    public void setMinWithdrawAmt(String minWithdrawAmt) {
        this.minWithdrawAmt = minWithdrawAmt;
    }

    public int getNumOfConfirmations() {
        return numOfConfirmations;
    }

    public void setNumOfConfirmations(int numOfConfirmations) {
        this.numOfConfirmations = numOfConfirmations;
    }

    public int getNumOfFastConfirmations() {
        return numOfFastConfirmations;
    }

    public void setNumOfFastConfirmations(int numOfFastConfirmations) {
        this.numOfFastConfirmations = numOfFastConfirmations;
    }

    public String getTransactFeeWithdraw() {
        return transactFeeWithdraw;
    }

    public void setTransactFeeWithdraw(String transactFeeWithdraw) {
        this.transactFeeWithdraw = transactFeeWithdraw;
    }

    public String getWithdrawFeeType() {
        return withdrawFeeType;
    }

    public void setWithdrawFeeType(String withdrawFeeType) {
        this.withdrawFeeType = withdrawFeeType;
    }

    public int getWithdrawPrecision() {
        return withdrawPrecision;
    }

    public void setWithdrawPrecision(int withdrawPrecision) {
        this.withdrawPrecision = withdrawPrecision;
    }

    public String getWithdrawQuotaPerDay() {
        return withdrawQuotaPerDay;
    }

    public void setWithdrawQuotaPerDay(String withdrawQuotaPerDay) {
        this.withdrawQuotaPerDay = withdrawQuotaPerDay;
    }

    public String getWithdrawQuotaPerYear() {
        return withdrawQuotaPerYear;
    }

    public void setWithdrawQuotaPerYear(String withdrawQuotaPerYear) {
        this.withdrawQuotaPerYear = withdrawQuotaPerYear;
    }

    public String getWithdrawQuotaTotal() {
        return withdrawQuotaTotal;
    }

    public void setWithdrawQuotaTotal(String withdrawQuotaTotal) {
        this.withdrawQuotaTotal = withdrawQuotaTotal;
    }

    public String getWithdrawStatus() {
        return withdrawStatus;
    }

    public void setWithdrawStatus(String withdrawStatus) {
        this.withdrawStatus = withdrawStatus;
    }

    @Override
    public String toString() {
        return "HuobiCurrency{" +
                "chain='" + chain + '\'' +
                ", displayName='" + displayName + '\'' +
                ", baseChain='" + baseChain + '\'' +
                ", baseChainProtocol='" + baseChainProtocol + '\'' +
                ", isDynamic=" + isDynamic +
                ", depositStatus='" + depositStatus + '\'' +
                ", maxTransactFeeWithdraw='" + maxTransactFeeWithdraw + '\'' +
                ", maxWithdrawAmt='" + maxWithdrawAmt + '\'' +
                ", minDepositAmt='" + minDepositAmt + '\'' +
                ", minTransactFeeWithdraw='" + minTransactFeeWithdraw + '\'' +
                ", minWithdrawAmt='" + minWithdrawAmt + '\'' +
                ", numOfConfirmations=" + numOfConfirmations +
                ", numOfFastConfirmations=" + numOfFastConfirmations +
                ", transactFeeWithdraw='" + transactFeeWithdraw + '\'' +
                ", withdrawFeeType='" + withdrawFeeType + '\'' +
                ", withdrawPrecision=" + withdrawPrecision +
                ", withdrawQuotaPerDay='" + withdrawQuotaPerDay + '\'' +
                ", withdrawQuotaPerYear='" + withdrawQuotaPerYear + '\'' +
                ", withdrawQuotaTotal='" + withdrawQuotaTotal + '\'' +
                ", withdrawStatus='" + withdrawStatus + '\'' +
                '}';
    }
}
