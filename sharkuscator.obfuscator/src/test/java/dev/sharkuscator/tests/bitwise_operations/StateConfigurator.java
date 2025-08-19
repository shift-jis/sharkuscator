package dev.sharkuscator.tests.bitwise_operations;

import java.util.Hashtable;

/**
 * BitwiseStateObjectインスタンスの設定や関連付けを行うコントローラーのようなクラス。
 * OperationTargetインターフェースを実装します。シングルトンです。
 */
public class StateConfigurator implements OperationTarget {

    // --- 静的メンバー ---
    // シングルトンインスタンス
    private static final StateConfigurator instance = new StateConfigurator();
    // 静的な真偽値フラグ。おそらくアサートなどの設定に使用
    private static boolean staticAssertEnabled;
    // --- インスタンスメンバー ---
    // OperationTargetオブジェクト間のマッピングを保持
    private final Hashtable<OperationTarget, OperationTarget> operationMap = new Hashtable<>();
    // このConfiguratorインスタンスが保持するint配列の状態
    private final int[] internalShiftMapState;
    // 関連付けられた次のOperationTarget
    private OperationTarget nextTarget;
    // このConfiguratorインスタンスが保持するlong状態値
    private long internalLongState;

    /**
     * コンストラクタ。
     * internalShiftMapStateを初期化し、BitwiseStateObjectの初期設定を開始します。
     */
    public StateConfigurator() {
        // 初期operationMapを設定
        setupInitialOperationMap();

        // internalShiftMapStateを初期化 (サイズ64)
        this.internalShiftMapState = new int[64];

        // internalShiftMapStateの一部にハードコードされた値を設定
        int[] iArr = this.internalShiftMapState;
        iArr[0] = -48;
        iArr[1] = -39;
        iArr[2] = -29;
        iArr[3] = -2;
        iArr[4] = -43;
        iArr[5] = 2;
        iArr[6] = -17;
        iArr[7] = -15;
        iArr[8] = -50;
        iArr[9] = -9;
        iArr[10] = -11;
        iArr[11] = -51;
        iArr[12] = -34;
        iArr[13] = -23;
        iArr[14] = -29;
        iArr[15] = -36;
        iArr[16] = -14;
        iArr[17] = -22;
        iArr[18] = 9;

        // BitwiseStateObjectの初期設定フェーズ1を開始
        BitwiseStateObject.setupInitialPoolAndConfigurator(this);
    }

    /**
     * 2つのOperationTargetオブジェクト間のマッピングを登録します。
     *
     * @param target1 値としてマップされるターゲット
     * @param target2 キーとして使用されるターゲット
     * @return 登録前にtarget2に対応していた値 (OperationTarget) またはnull
     */
    public static OperationTarget mapOperationTargets(OperationTarget target1, OperationTarget target2) {
        // シングルトンインスタンスを使ってマッピングを登録
        return instance.registerOperationMapping(target2, target1);
    }

    /**
     * operationMap にOperationTarget間のマッピングを登録します。
     *
     * @param key   マップのキーとなるOperationTarget
     * @param value キーに対応する値となるOperationTarget
     * @return 登録前にkeyに対応していた値 (OperationTarget) またはnull
     */
    private OperationTarget registerOperationMapping(OperationTarget key, OperationTarget value) {
        OperationTarget existingValue = this.operationMap.get(key);
        // 新しいマッピングを登録
        this.operationMap.put(value, key);
        return existingValue;
    }

    /**
     * Configuratorの設定フェーズ1を実行します。
     * 特定のBitwiseStateObjectインスタンスのシフトマップを設定し、BitwiseStateObjectのstaticShiftMapを更新します。
     */
    void applyConfiguratorSettingsPhase1() {
        configureSpecificA8ShiftMaps();
        // BitwiseStateObjectのstaticShiftMapをこのConfiguratorが定義するマップで上書き
        BitwiseStateObject.applyConfiguratorShiftMap(this);
    }

    /**
     * Configuratorの設定フェーズ2を実行します。
     * 特定のBitwiseStateObjectインスタンスの関連付けや補助状態値を設定します。
     */
    void applyConfiguratorSettingsPhase2() {
        configureSpecificA8LinksAndAuxStatesPhase1();
        configureSpecificA8LinksAndAuxStatesPhase2();
    }

    // --- OperationTarget インターフェースの実装 ---

    /**
     * @inheritDoc
     */
    @Override // me.rerere.matrix.internal.un
    public void setNextOperationTarget(OperationTarget nextTarget) {
        // 自分自身を関連付けようとした場合は何もしない
        if (this != nextTarget) {
            // まだ関連付けられていなければ直接設定
            if (this.nextTarget == null) {
                this.nextTarget = nextTarget;
            } else {
                // すでに関連付けられていれば、そのターゲットに設定を委譲 (チェインの最後に追加)
                this.nextTarget.setNextOperationTarget(nextTarget);
            }
        }
    }

    /**
     * @inheritDoc 入力long値に対応するBitwiseStateObjectインスタンスを取得し、操作を実行させ、
     * このConfiguratorのint配列状態をそのBitwiseStateObjectにコピーします。
     */
    @Override // me.rerere.matrix.internal.un
    public long processLongValue(long inputValue) {
        // 入力値に対応するBitwiseStateObjectインスタンスをプールから取得または作成
        OperationTarget targetA8 = BitwiseStateObject.getInstanceFromPool(inputValue);

        // 取得したBitwiseStateObjectインスタンスの操作メソッドを実行
        long result = targetA8.processLongValue(inputValue);

        // !!! 重要な処理 !!!
        // このConfiguratorインスタンス自身のinternalShiftMapStateの内容を、
        // 取得したBitwiseStateObjectインスタンスのinstanceShiftMapに上書きコピー
        System.arraycopy(this.internalShiftMapState, 0, targetA8.getIntArrayState(), 0, 64);

        // 関連付けられた次のOperationTargetがあれば、処理を委譲 (チェイン)
        if (this.nextTarget != null) {
            this.nextTarget.processLongValue(inputValue);
        }

        return result; // BitwiseStateObjectの操作結果を返す
    }

    /**
     * @inheritDoc BitwiseStateObjectの静的なシフトマップ配列を返します。
     */
    @Override // me.rerere.matrix.internal.un
    public int[] getIntArrayState() {
        return BitwiseStateObject.staticShiftMap;
    }

    /**
     * @inheritDoc オブジェクトの等価性をidentityHashCodeに基づいて比較します。
     * OperationTargetプールソートで使用される可能性のある比較メソッドですが、
     * identityHashCodeに基づく順序は通常不定です。
     */
    @Override // me.rerere.matrix.internal.un
    public boolean isOrderedBefore(OperationTarget other) {
        // 同一オブジェクトならtrue
        if (this == other) {
            return true;
        }
        // 比較対象がStateConfiguratorインスタンスであり、identityHashCodeに基づく順序比較
        return (other instanceof StateConfigurator) && System.identityHashCode(this) - System.identityHashCode(other) <= 0;
    }

    /**
     * @inheritDoc このConfiguratorインスタンスの内部的なlong状態値を設定します。
     */
    @Override // me.rerere.matrix.internal.un
    public void setLongState(long stateValue) {
        this.internalLongState = stateValue;
    }

    // --- 初期設定メソッド (ハードコードされた値を設定) ---

    /**
     * operationMap に初期マッピングをハードコードされたlong値をキー/値として設定します。
     */
    private void setupInitialOperationMap() {
        Hashtable<OperationTarget, OperationTarget> map = this.operationMap;
        // 特定のlong値に対応するa8インスタンスを取得し、その間のマッピングを登録
        map.put(BitwiseStateObject.getInstanceFromPool(7060890996790174117L), BitwiseStateObject.getInstanceFromPool(2402921654418728089L));
        map.put(BitwiseStateObject.getInstanceFromPool(1635333765710492280L), BitwiseStateObject.getInstanceFromPool(-1397489961321305194L));
        map.put(BitwiseStateObject.getInstanceFromPool(725698457437613915L), BitwiseStateObject.getInstanceFromPool(-5804415106389080309L));
        map.put(BitwiseStateObject.getInstanceFromPool(-1960690038599270855L), BitwiseStateObject.getInstanceFromPool(3513649145373666145L));
        map.put(BitwiseStateObject.getInstanceFromPool(-5708816057880904857L), BitwiseStateObject.getInstanceFromPool(-2561958028683225933L));
        map.put(BitwiseStateObject.getInstanceFromPool(-1571286813478484545L), BitwiseStateObject.getInstanceFromPool(-1536234111596572583L));
        map.put(BitwiseStateObject.getInstanceFromPool(3944371215049454555L), BitwiseStateObject.getInstanceFromPool(-7407498423409655102L));
    }

    /**
     * 特定のBitwiseStateObjectインスタンスのinstanceShiftMapに、
     * ハードコードされたint配列の値を設定します。Configurator設定フェーズ1の一部です。
     */
    private void configureSpecificA8ShiftMaps() {
        // long値 8884789152875820361L に対応するa8インスタンスを取得し、そのint配列状態を設定
        int[] shiftMap1 = BitwiseStateObject.getInstanceFromPool(8884789152875820361L).getIntArrayState();
        shiftMap1[0] = -43;
        shiftMap1[1] = -26;
        shiftMap1[2] = -27;
        shiftMap1[3] = -32;
        shiftMap1[4] = -7;
        shiftMap1[5] = -46;
        shiftMap1[6] = -24;
        shiftMap1[7] = -19;
        shiftMap1[8] = -36;
        shiftMap1[9] = -1;
        shiftMap1[10] = 1;
        shiftMap1[11] = 7;
        shiftMap1[12] = -16;
        shiftMap1[13] = -21;
        shiftMap1[14] = -40;
        shiftMap1[15] = -24;
        shiftMap1[16] = -39;
        shiftMap1[17] = -23;
        shiftMap1[18] = -20;
        shiftMap1[19] = -28;
        shiftMap1[20] = -29;
        shiftMap1[21] = -40;
        shiftMap1[22] = -10;
        shiftMap1[23] = -19;
        shiftMap1[24] = -32;
        shiftMap1[25] = -34;
        shiftMap1[26] = 19;
        shiftMap1[27] = 26;
        shiftMap1[28] = 16;
        shiftMap1[29] = 27;
        shiftMap1[30] = 24;
        shiftMap1[31] = -5;
        shiftMap1[32] = 10;
        shiftMap1[33] = -8;
        shiftMap1[34] = 21;
        shiftMap1[35] = 32;
        shiftMap1[36] = 5;
        shiftMap1[37] = -8;
        shiftMap1[38] = 20;
        shiftMap1[39] = 24;
        shiftMap1[40] = 23;
        shiftMap1[41] = 8;
        shiftMap1[42] = 19;
        shiftMap1[43] = 43;
        shiftMap1[44] = 36;
        shiftMap1[45] = 8;
        shiftMap1[46] = -16;
        shiftMap1[47] = 28;
        shiftMap1[48] = -9;
        shiftMap1[49] = 29;
        shiftMap1[50] = -10;
        shiftMap1[51] = 46;
        shiftMap1[52] = -6;
        shiftMap1[53] = -10;
        shiftMap1[54] = 40;
        shiftMap1[55] = 39;
        shiftMap1[56] = 32;
        shiftMap1[57] = 9;
        shiftMap1[58] = 6;
        shiftMap1[59] = 34;
        shiftMap1[60] = 10;
        shiftMap1[61] = 40;
        shiftMap1[62] = 16;
        shiftMap1[63] = 10;

        // long値 369631727363734033L に対応するa8インスタンスを取得し、そのint配列状態を設定
        int[] shiftMap2 = BitwiseStateObject.getInstanceFromPool(369631727363734033L).getIntArrayState();
        shiftMap2[0] = -49;
        shiftMap2[1] = -7;
        shiftMap2[2] = -33;
        shiftMap2[3] = -60;
        shiftMap2[4] = -20;
        shiftMap2[5] = -57;
        shiftMap2[6] = -31;
        shiftMap2[7] = -11;
        shiftMap2[8] = 7;
        shiftMap2[9] = -36;
        shiftMap2[10] = -17;
        shiftMap2[11] = -25;
        shiftMap2[12] = -19;
        shiftMap2[13] = -6;
        shiftMap2[14] = -27;
        shiftMap2[15] = -6;
        shiftMap2[16] = -1;
        shiftMap2[17] = 1;
        shiftMap2[18] = 11;
        shiftMap2[19] = 6;
        shiftMap2[20] = -3;
        shiftMap2[21] = 6;
        shiftMap2[22] = -8;
        shiftMap2[23] = 3;
        shiftMap2[24] = 20;
        shiftMap2[25] = -14;
        shiftMap2[26] = -12;
        shiftMap2[27] = 17;
        shiftMap2[28] = -33;
        shiftMap2[29] = -17;
        shiftMap2[30] = 8;
        shiftMap2[31] = 19;
        shiftMap2[32] = -11;
        shiftMap2[33] = -26;
        shiftMap2[34] = -17;
        shiftMap2[35] = 33;
        shiftMap2[36] = 25;
        shiftMap2[37] = 31;
        shiftMap2[38] = 12;
        shiftMap2[39] = 14;
        shiftMap2[40] = -2;
        shiftMap2[41] = 27;
        shiftMap2[42] = 2;
        shiftMap2[43] = 11;
        shiftMap2[44] = -9;
        shiftMap2[45] = 36;
        shiftMap2[46] = 17;
        shiftMap2[47] = -11;
        shiftMap2[48] = -7;
        shiftMap2[49] = 49;
        shiftMap2[50] = -6;
        shiftMap2[51] = 17;
        shiftMap2[52] = -5;
        shiftMap2[53] = 9;
        shiftMap2[54] = -6;
        shiftMap2[55] = 7;
        shiftMap2[56] = 6;
        shiftMap2[57] = 5;
        shiftMap2[58] = 11;
        shiftMap2[59] = 26;
        shiftMap2[60] = 6;
        shiftMap2[61] = 33;
        shiftMap2[62] = 57;
        shiftMap2[63] = 60;
    }

    /**
     * 特定のBitwiseStateObjectインスタンスのnextTargetやauxiliaryStateValueに、
     * ハードコードされた値を設定します。Configurator設定フェーズ2の一部です。
     */
    private void configureSpecificA8LinksAndAuxStatesPhase1() {
        // 特定のlong値に対応するa8インスタンスを取得し、他のインスタンスをnextTargetとして設定
        BitwiseStateObject.getInstanceFromPool(-415506508291524505L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-1659505232145662229L));
        BitwiseStateObject.getInstanceFromPool(6381026896161261201L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-1961087605857616349L));
        BitwiseStateObject.getInstanceFromPool(3605445749205812998L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(9095568841524085814L));
        BitwiseStateObject.getInstanceFromPool(7075530383311786512L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(1207142392983814791L));
        BitwiseStateObject.getInstanceFromPool(7444715656373077015L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(7771506422308185378L));
        BitwiseStateObject.getInstanceFromPool(-7203084910620893678L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(1652397624374838819L));
        BitwiseStateObject.getInstanceFromPool(-4842650027116825372L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-344063238400462139L));
        BitwiseStateObject.getInstanceFromPool(-2653640933810573416L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-7715864761595261001L));
        BitwiseStateObject.getInstanceFromPool(-2875227334403929782L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-2659136320193334400L));
        BitwiseStateObject.getInstanceFromPool(-5395048452610556428L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-6263000878920363635L));
        BitwiseStateObject.getInstanceFromPool(8434679570584282501L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-2840765547873874364L));
        BitwiseStateObject.getInstanceFromPool(5023640333135547789L).setNextOperationTarget(BitwiseStateObject.getInstanceFromPool(-8795101054966375184L));

        // 特定のlong値に対応するa8インスタンスを取得し、auxiliaryStateValueを設定
        BitwiseStateObject.getInstanceFromPool(3846868707100873188L).setLongState(4210829273732580001L);
        BitwiseStateObject.getInstanceFromPool(-4221060486282509982L).setLongState(6380295840476488995L);
        BitwiseStateObject.getInstanceFromPool(-5055890893819145184L).setLongState(7457614186160455664L);
        BitwiseStateObject.getInstanceFromPool(2249977293595640047L).setLongState(95693881760280059L);
        BitwiseStateObject.getInstanceFromPool(-5649527959289256653L).setLongState(-3964880038533436923L);
        BitwiseStateObject.getInstanceFromPool(-6490016739292135264L).setLongState(1588559428557126505L);
        BitwiseStateObject.getInstanceFromPool(990101817072298284L).setLongState(3890375459582411831L);
        BitwiseStateObject.getInstanceFromPool(-6983204479088361317L).setLongState(4182937171651559868L);
        BitwiseStateObject.getInstanceFromPool(3775449439213854367L).setLongState(634539013080956968L);
    }

    /**
     * 特定のBitwiseStateObjectインスタンスのauxiliaryStateValueに、
     * ハードコードされた値を設定します。Configurator設定フェーズ2の一部です。
     */
    private void configureSpecificA8LinksAndAuxStatesPhase2() {
        // 特定のlong値に対応するa8インスタンスを取得し、auxiliaryStateValueを設定
        BitwiseStateObject.getInstanceFromPool(5469950181187501788L).setLongState(48462590171438865L);
        BitwiseStateObject.getInstanceFromPool(8726895660942634014L).setLongState(2110356444878165647L);
        BitwiseStateObject.getInstanceFromPool(8305822644883947303L).setLongState(7808129931018889574L);
        BitwiseStateObject.getInstanceFromPool(-2358209902483213216L).setLongState(4961476461731161804L);
        BitwiseStateObject.getInstanceFromPool(7233481744127249234L).setLongState(4468805545543261805L);
        BitwiseStateObject.getInstanceFromPool(-2431466173634241308L).setLongState(-3941869365270336841L);
        BitwiseStateObject.getInstanceFromPool(2986963306640032647L).setLongState(4769836481242188154L);
        BitwiseStateObject.getInstanceFromPool(5772505595825520330L).setLongState(1586111586094928102L);
        BitwiseStateObject.getInstanceFromPool(-60258799057391452L).setLongState(44061038713483395L);
    }
}
