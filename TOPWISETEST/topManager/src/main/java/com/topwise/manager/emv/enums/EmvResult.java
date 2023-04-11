package com.topwise.manager.emv.enums;

/**
 * 创建日期：2021/6/10 on 16:55
 * 描述:
 * 作者:wangweicheng
 */
public enum EmvResult {
    //0
    APPROVED(0,"APPROVED"),
    OFFLINE_APPROVED(1,"OFFLINE APPROVED"),
    TRANS_STOP(2,"TRANS STOP"),
    //Offline Declined
    TRANS_DENIED(3,"DECLINED"),
    ONLINE_CARD_DENIED(4,"ONLINE CARD DENIED"),
    ABORT_TERMINATED(5,"ABORT TERMINATED"),
    ARQC(6,"ARQC"),
    SIMPLE_FLOW_END(7,"SIMP LEFLOW END"),

    OK(8,"NEXT"),

    //-6666AppFinalSelect
    IC_INIT_ERR(-6666,"EMV INIT Failed"),
    IC_SET_KCONFIG_ERR(-6665,"SET KERNEL CONFIG Failed"),
    IC_SET_TERMINAL_INFO_ERR(-6664,"SET TERMINAL INFO Failed"),
    IC_SET_SUPPORT_PBOC_ERR(-6663,"SET TERMINAL INFO Failed"),
    IC_AIDS_LIST_NULL_ERR(-6662,"Entering AIDS is empty"),
    IC_CAPKS_LIST_NULL_ERR(-6661,"Entering CAKPS is empty"),
    IC_BUILD_CANDIDATE_ERR(-6660,"Building the Candidate List Failed"),
    IC_SELECT_AIDS_ERR(-6659,"Select Aids Failed"),
    IC_GET_CARD_LIST_ERR(-6658,"Get Card List Failed"),
    IC_AID_FINAL_SELECT_ERR(-6657,"Aid Final Select Failed"),
    IC_INPUT_AMOUNT_ERR(-6656,"Input Amount Failed"),
    IC_SET_TRANS_DATA_ERR(-6654,"Set Trtans Data Failed"),
    IC_SET_AID_PARAMS_ERR(-6653,"Set Aid Params Failed"),
    IC_FINAL_AID_SELECT_ERR(-6652,"Final Ais Select Failed"),
    IC_GPO_ERR(-6651,"GPO Failed"),
    IC_READ_DATA_ERR(-6650,"READ DATA Failed"),
    IC_GET_PAN_ERR(-6649,"Get Pan Failed"),
    IC_CONFIRM_PAN_CANCEL(-6648,"Confirm Pan Cancel"),
    IC_READ_CAPK_ERR(-6647,"Read Capk Failed"),
    IC_OFFLINE_AUTH_ERR(-6646,"Offline Auth Failed"),
    IC_TERMINAL_RISK_MANAGER_ERR(-6645,"Terminal Risk Management Failed"),
    IC_TPROCE_RESTRICTION_ERR(-6644,"Processing Restrictions Failed"),
    IC_CVM_ERR(-6643,"CVM Failed"),
    IC_TERMINAL_ACTION_ANALYZE_ERR(-6642,"Terminal Action Analyze Failed"),
    IC_EMV_PROCESS_ERR(-6641,"EMV Process Failed"),
    IC_EMV_NO_ACCEPTED(-6640,"EMV_NO_ACCEPTED"),
    IC_EMV_NO_MORE_DATA(-6639,"EMV_NO_MORE_DATA"),
    IC_EMV_ICC_ERROR(-6638,"EMV_ICC_ERROR"),
    IC_EMV_FALL_BACK(-6637,"EMV_ICC_FALLBACK"),
    IC_EMV_APP_BLOCKED(-6636,"EMV APP BLOCKED"),
    IC_EMV_NO_APP(-6635,"EMV NO APP"),
    IC_EMV_INVALID_RESPONSE(-6634,"EMV INVALID RESPONSE"),
    IC_EMV_INVALID_TLV(-6633,"EMV INVALID TLV"),
    IC_EMV_DATA_NOT_EXISTS(-6632,"EMV DATA NOT EXISTS"),

    //-5555 RF TermActionAnalyze
    RF_INIT_ERR(-5555,"RF INIT Failed"),
    RF_AIDS_LIST_NULL_ERR(-5554,"Entering AIDS is empty"),
    RF_TRANS_PARAM_NULL_ERR(-5553,"InputParam is empty"),
    RF_PRE_PROCESS_ERR(-5552,"PRE PROCESS Failed"),
    RF_BUILD_CANDIDATA_ERR(-5551,"Building the Candidate Failed"),
    RF_PROCESS_ERR(-5550,"ContactLess Process Failed"),
    RF_AID_FINAL_SELECT_ERR(-5549,"Aid Final Select Failed"),
    RF_PRE_PROCESS_RETURN_ERR(-5548,"PRE PROCESS return Failed"),
    RF_NOT_SUPPORT_KERNAL_ERR(-5547,"Not Support Kernal"),
    RF_AID_FINAL_SELECT_AGAIN_ERR(-5546,"Aid Final Select Again Kernal"),
    RF_KERNAL_INIT_ERR(-5545,"Kernal init Failed"),
    RF_SET_CALL_BACK_ERR(-5544,"set Callback Failed"),
    RF_DEL_CAND_LIST_ERR(-5543,"Del Cand List Failed"),
    RF_GET_AID_ERR(-5542,"set Callback is empty"),
    RF_MATCH_AIDLIST_ERR(-5541,"Match Aids Failed"),
    RF_PRE_TTQ_ERR(-5540,"TTQ is empty"),
    RF_CDCVM_SECOND_READ_CARD(-5539,"CDCVM second read card"),
    RF_TRANS_AGAIN_CHECK_CARD(-5538,"Check Card Again"),
    RF_READ_DATA(-5537,"Read Data Failed"),
    RF_CONFIRM_CARDNO_CANCEL(-5536,"Confirm CardNo calcel"),
    RF_INPUT_PIN_CANCEL(-5535,"Cancel Password"),
    RF_MC_PRO(-5534,"trans Proc MChip Failed"),
    RF_OFFLINE_AUTH_ERR(-5533,"Offline Auth Failed"),
    RF_CHECK_BACK_ERR(-5532,"Check Back Failed"),
    RF_CHECK_OTHER_CONTACT(-5531,"User Other Cantact"),
    ;

    private int result;
    private String message;

    EmvResult(int result, String message) {
        this.result = result;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Code= " + result +
                "\nmessage= " +message ;
    }
}
