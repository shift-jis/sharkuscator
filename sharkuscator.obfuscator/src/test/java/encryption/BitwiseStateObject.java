package encryption;

import java.util.Vector;

/**
 * long値を内部状態として持ち、静的およびインスタンスレベルのint配列とlong配列を用いたビット操作を行うクラス。
 * インスタンスのプール管理とソート機能も提供します。OperationTargetインターフェースを実装します。
 */
public class BitwiseStateObject implements OperationTarget {

    // プール操作のスレッドセーフ化に使用される可能性のあるロックオブジェクト
    private static final Object poolLock = new Object();
    // プールインデックス計算に使用されるビット操作の定数
    private static final int SHIFT_LIMIT_H = 51;
    // プールソート(マージソート)の再帰深度制限
    private static final int SORT_DEPTH_N = 17;
    // プールサイズがこの値の倍数になるたびにstaticShiftMapをクローンする条件
    private static final int POOL_BATCH_L = 128;
    // --- 静的メンバー ---
    // ビット操作で使用される静的なシフト量配列。Configuratorによって変更されることがある。
    public static int[] staticShiftMap;
    // BitwiseStateObjectインスタンスのプール
    private static final Vector<BitwiseStateObject> instancePool;
    // 関連付けられた任意のオブジェクトを保持するVector
    private static final Vector<Object> associatedObjects;
    // instancePool に初期状態で格納されているインスタンスの数
    private static int initialPoolSize;
    // ビット操作で使用される64個のビットマスク (1L << i)
    private static final long[] staticBitMasks = new long[64];

    // --- 静的初期化ブロック ---
    static {
        long bit = 1;
        // staticBitMasks を初期化 (1, 2, 4, 8, ...)
        for (int i = 0; i < 64; i++) {
            staticBitMasks[i] = bit;
            bit <<= 1;
        }
        // associatedObjects と instancePool を初期化
        associatedObjects = new Vector<>();
        instancePool = new Vector<>();

        // 初期プールを生成
        populateInitialPool();

        // initialPoolSize を設定
        initialPoolSize = instancePool.size();

        // 初期プールをソート
        sortInstancePool();
    }

    // --- インスタンスメンバー ---
    private long stateValue; // このインスタンスの主要なlong状態値
    private OperationTarget nextTarget; // 関連付けられた次のOperationTarget
    private long auxiliaryStateValue; // 補助的なlong状態値
    private final int[] instanceShiftMap; // このインスタンスで使用されるシフトマップint配列
    private final long[] instanceBitMasks; // このインスタンスで使用されるビットマスクlong配列 (通常は staticBitMasks と同じ)

    /**
     * コンストラクタ。主要なlong状態値を設定します。
     * インスタンスのシフトマップとビットマスクは静的メンバーを参照します。
     *
     * @param stateValue 初期状態値
     */
    private BitwiseStateObject(long stateValue) {
        this.stateValue = stateValue;
        // デフォルトでは静的マップを参照
        this.instanceShiftMap = staticShiftMap;
        this.instanceBitMasks = staticBitMasks;
    }

    /**
     * 新しいBitwiseStateObjectインスタンスを作成し、オプションで関連オブジェクトを追加します。
     * 作成されたインスタンスは内部的にリンクされますが、このリンクが何を意味するかは不明です (fy.aを使用)。
     *
     * @param value1           値1 (おそらくインスタンス状態の生成に使用)
     * @param value2           値2 (おそらくインスタンス状態の生成に使用)
     * @param associatedObject このインスタンスに関連付けられるオブジェクト (nullでも可)
     * @return リンクされたOperationTarget (おそらく新しいBitwiseStateObjectインスタンス)
     */
    public static OperationTarget createAndRegisterInstance(long value1, long value2, Object associatedObject) {
        // 値1が正であることのチェック (fy.aはおそらくfyクラスの静的assert)
        // assert value1 > 0; // もしfy.aがアサートなら

        // 2つのlong値から新しいBitwiseStateObjectインスタンスを作成し、何らかの形で関連付ける
        // fy.a(d(value1), d(value2)) はfy.registerOperationMapping(createNewInstance(value1), createNewInstance(value2)) と解釈できる
        OperationTarget linkedTarget = StateConfigurator.mapOperationTargets(createNewInstance(value1), createNewInstance(value2));

        // 関連オブジェクトがあれば追加
        if (associatedObject != null) {
            associatedObjects.addElement(associatedObject);
        }
        return linkedTarget;
    }

    /**
     * 指定されたlong値に対応するBitwiseStateObjectインスタンスをプールから取得または作成します。
     *
     * @param value 検索または生成に使用するlong値
     * @return プールから取得または新しく作成されたBitwiseStateObjectインスタンス
     */
    static OperationTarget getInstanceFromPool(long value) {
        // long値から、staticShiftMapとstaticBitMasksを使ってビット操作結果を計算し、これをプールインデックスとして使用
        int poolIndex = (int) applyShiftAndMask(value, SHIFT_LIMIT_H, 63, staticShiftMap, staticBitMasks);

        // インデックスが現在のプールサイズ未満であれば、既存のインスタンスを返す
        if (poolIndex < instancePool.size()) {
            return instancePool.elementAt(poolIndex);
        }

        // プールサイズがPOOL_BATCH_Lの倍数であれば、staticShiftMapをクローンして参照を更新
        // これは、ConfiguratorがstaticShiftMapを変更した場合に、それ以降に作成されるインスタンスが
        // 新しいマップのクローンを使用するようにするための処理かもしれない。
        if (instancePool.size() % POOL_BATCH_L == 0) {
            staticShiftMap = staticShiftMap.clone();
        }

        // 新しいインスタンスを作成し、プールに追加
        BitwiseStateObject newInstance = new BitwiseStateObject(value);
        instancePool.addElement(newInstance);

        return newInstance;
    }

    /**
     * 新しいBitwiseStateObjectインスタンスをシンプルに作成します。
     * コンストラクタ呼び出しのラッパーです。
     *
     * @param value 初期状態値
     * @return 新しいBitwiseStateObjectインスタンス
     */
    private static OperationTarget createNewInstance(long value) {
        return new BitwiseStateObject(value);
    }

    /**
     * 初期プールサイズを設定し、プールをソートし、Configuratorの初期化フェーズ1を開始します。
     * StateConfiguratorのコンストラクタから呼び出されます。
     *
     * @param configurator StateConfiguratorインスタンス
     */
    static void setupInitialPoolAndConfigurator(StateConfigurator configurator) {
        initialPoolSize = instancePool.size();
        sortInstancePool();
        configurator.applyConfiguratorSettingsPhase1();
    }

    /**
     * staticShiftMap をハードコードされた値で設定し直し、Configuratorの初期化フェーズ2を開始します。
     * StateConfiguratorのapplyConfiguratorSettingsPhase1から呼び出されます。
     *
     * @param configurator StateConfiguratorインスタンス
     */
    static void applyConfiguratorShiftMap(StateConfigurator configurator) {
        // staticShiftMap をハードコードされた値で上書き
        staticShiftMap = new int[]{-40, -44, -2, -23, 2, -43, -36, -8, -53, -35, -11, -6, -18, -5, -40, 8, -22, 6, 5, -36, -7, 11, -3, -9, -27, 3, 23, 7, -13, -5, 18, -32, 9, -19, 5, -22, -24, -22, 22, -8, 40, 13, 36, -3, 35, 44, 3, 8, 43, -13, -3, 27, 19, 3, 40, 36, -2, 22, 2, 22, 24, 53, 13, 32};
        configurator.applyConfiguratorSettingsPhase2();
    }

    // --- OperationTarget インターフェースの実装 ---

    /**
     * 入力long値に対し、shiftMapとmasksを使って複雑なビット操作を行い、指定されたビット範囲の値を抽出します。
     * 各ビット位置に対して、対応するマスクを適用し、そのビットが立っていればshiftMapに従ってシフトし、結果をORしていきます。
     * 最後に指定されたビット範囲(startBitからendBitまでを含む)のみを抽出して返します。
     *
     * @param value    入力long値
     * @param startBit 抽出するビット範囲の開始位置 (0を含む)
     * @param endBit   抽出するビット範囲の終了位置 (63を含む)
     * @param shiftMap 各ビットのシフト量を定義するint配列
     * @param masks    各ビットに対応するビットマスクlong配列 (通常は1L << i の形式)
     * @return 操作・抽出されたビットフィールド値
     */
    private static long applyShiftAndMask(long value, int startBit, int endBit, int[] shiftMap, long[] masks) {
        long resultValue = 0;
        int mapLength = shiftMap.length;

        // 各ビット位置に対して操作を適用
        for (int i = 0; i < mapLength; i++) {
            // i番目のビットマスクを適用して、入力値のi番目のビットが立っているかを確認
            long maskedBit = value & masks[i];
            int shiftAmount = shiftMap[i];

            // i番目のビットが立っている場合のみシフト操作
            if (maskedBit != 0) {
                if (shiftAmount > 0) {
                    // 正のシフト量は右シフト (符号なし)
                    maskedBit >>>= shiftAmount;
                } else if (shiftAmount < 0) {
                    // 負のシフト量は左シフト (符号付き)
                    // Javaでは負のシフト量はシフト量の絶対値をマスクしたものとして扱われるが、
                    // ここでは (~shiftAmount) + 1 という計算をしている。
                    // これは shiftAmount の絶対値に等しい。
                    // したがって、負のシフト量は、シフト量の絶対値分だけ左シフトする処理。
                    maskedBit <<= (~shiftAmount) + 1;
                }
                // 操作結果をORで合成
                resultValue |= maskedBit;
            }
        }

        long extractedField = resultValue;
        int totalBits = 64;
        int bitRangeLength = endBit - startBit + 1;

        // 指定されたビット範囲のみを抽出する
        // まず、抽出したい範囲の開始ビットが最上位(63)に来るように左シフト
        int shiftLeft = (totalBits - 1) - endBit;
        if (shiftLeft > 0) {
            extractedField <<= shiftLeft;
        }

        // 次に、抽出したい範囲の長さだけ右シフト (符号なし)
        // 開始ビットが目的の位置に来るようにシフト
        // 元コードの (i2 + 64 - 1) - i3 = startBit + 63 - endBit
        // これは、最初の左シフト後の開始ビット位置を最下位(0)に持ってくるための右シフト量。
        int shiftRightToAlign = ((startBit + totalBits) - 1) - endBit;
        if (shiftRightToAlign > 0) {
            // 符号なし右シフト
            extractedField >>>= shiftRightToAlign;
        }

        return extractedField;
    }

    /**
     * instancePool を isOrderedBefore 基準でソートします (マージソート)。
     */
    private static void sortInstancePool() {
        int size = instancePool.size();
        // ソートのための一時的なVectorを作成
        Vector<BitwiseStateObject> tempVector = new Vector<>(size);
        for (int i = 0; i < size; i++) {
            tempVector.addElement(instancePool.elementAt(i));
        }
        // マージソートの開始
        mergeSortDivide(0, instancePool.size() - 1, instancePool, tempVector, 0);
    }

    /**
     * マージソートの分割ステップ (再帰)。
     *
     * @param low          ソート範囲の開始インデックス
     * @param high         ソート範囲の終了インデックス
     * @param sourceVector ソート対象のVector
     * @param tempVector   一時作業用のVector
     * @param depth        再帰深度
     */
    private static void mergeSortDivide(int low, int high, Vector<BitwiseStateObject> sourceVector, Vector<BitwiseStateObject> tempVector, int depth) {
        // 範囲が1要素以下ならソート不要
        if (low < high) {
            // 中央のインデックスを計算
            int mid = low + ((high - low) / 2);
            // 再帰深度をインクリメント
            int nextDepth = depth + 1;

            // 再帰深度が制限未満であれば、左右半分を再帰的にソート
            if (nextDepth < SORT_DEPTH_N) {
                mergeSortDivide(low, mid, sourceVector, tempVector, nextDepth);
                mergeSortDivide(mid + 1, high, sourceVector, tempVector, nextDepth);
            }

            // ソートされた左右半分をマージ
            mergeSortMerge(low, mid, high, sourceVector, tempVector);
        }
    }

    /**
     * マージソートのマージステップ。
     *
     * @param low          左半分の開始インデックス
     * @param mid          左半分の終了インデックス
     * @param high         右半分の終了インデックス
     * @param sourceVector ソート対象のVector (マージ結果を格納)
     * @param tempVector   一時作業用のVector (ソート済みの左右半分がコピーされている)
     */
    private static void mergeSortMerge(int low, int mid, int high, Vector<BitwiseStateObject> sourceVector, Vector<BitwiseStateObject> tempVector) {
        int leftPtr = low;
        int rightPtr = mid + 1;
        int currentPtr = low;

        // 処理対象範囲を一時Vectorにコピー
        for (int i = low; i <= high; i++) {
            tempVector.setElementAt(sourceVector.elementAt(i), i);
        }

        // 一時Vectorの左右半分から、isOrderedBefore基準で小さい方を元のVectorに戻していく
        while (leftPtr <= mid && rightPtr <= high) {
            BitwiseStateObject leftElement = tempVector.elementAt(leftPtr);
            BitwiseStateObject rightElement = tempVector.elementAt(rightPtr);
            BitwiseStateObject nextElement;

            // isOrderedBefore 基準で比較
            if (leftElement.isOrderedBefore(rightElement)) {
                nextElement = leftElement;
                leftPtr++;
            } else {
                nextElement = rightElement;
                rightPtr++;
            }
            // マージ結果を元のVectorに格納
            sourceVector.setElementAt(nextElement, currentPtr);
            currentPtr++;
        }

        // 残った要素を元のVectorにコピー
        while (leftPtr <= mid) {
            sourceVector.setElementAt(tempVector.elementAt(leftPtr), currentPtr);
            currentPtr++;
            leftPtr++;
        }
        // (右半分に要素が残っている場合は、すでに元のVectorの正しい位置にあるためコピー不要)
    }

    /**
     * staticShiftMap を設定し、初期のBitwiseStateObjectインスタンスをプールに追加します。
     * 静的イニシャライザから呼び出されます。
     */
    private static void populateInitialPool() {
        // staticShiftMap にハードコードされた初期値を設定
        staticShiftMap = new int[]{-49, -7, -33, -60, -20, -57, -31, -11, 7, -20, -21, -15, -8, -40, -7, -26, -6, -44, 11, -23, 8, 7, 6, -9, 20, -13, 15, -31, -29, 20, -3, 21, 9, 3, -6, 33, -16, 31, 13, -21, 6, 26, 23, -1, 1, -6, -8, -9, -7, 49, -9, 6, 16, 40, 8, 7, 9, 29, 31, 9, 21, 44, 57, 60};

        // ハードコードされたlong値を持つBitwiseStateObjectインスタンスをプールに追加
        instancePool.addElement(new BitwiseStateObject(4571342691005442043L));
        instancePool.addElement(new BitwiseStateObject(1486187967619834258L));
        instancePool.addElement(new BitwiseStateObject(-4833259550567966308L));
        instancePool.addElement(new BitwiseStateObject(5853013420897962686L));
        instancePool.addElement(new BitwiseStateObject(4613999590356761709L));
        instancePool.addElement(new BitwiseStateObject(-5958123907058260959L));
    }

    // --- 標準メソッドのオーバーライド ---

    /**
     * @inheritDoc
     */
    @Override
    public long processLongValue(long inputValue) {
        // 現在のstateValueに対し、インスタンスのシフトマップとマスクを使ってビット操作を行い、結果を取得
        long bitOpResult = applyShiftAndMask(this.stateValue, 8, 55, this.instanceShiftMap, this.instanceBitMasks);

        // stateValue を 入力値 と 補助状態値 で XOR 更新
        this.stateValue ^= inputValue ^ this.auxiliaryStateValue;

        // 関連付けられた次のOperationTargetがあれば、処理を委譲 (チェイン)
        if (this.nextTarget != null) {
            this.nextTarget.processLongValue(inputValue);
        }

        return bitOpResult; // ビット操作結果を返す
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setLongState(long stateValue) {
        this.auxiliaryStateValue = stateValue;
    }


    // --- ユーティリティメソッド (インスタンスレベルのビットフィールド抽出) ---

    /**
     * @inheritDoc
     */
    @Override
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
     * @inheritDoc
     */
    @Override
    public int[] getIntArrayState() {
        return this.instanceShiftMap;
    }

    // --- 静的ユーティリティメソッド (コアビット操作) ---

    /**
     * @inheritDoc プールソートで使用される比較メソッド。
     * 自身のstateValueと比較対象のstateValueに対し、特定のビットフィールド(56-63ビット目)を抽出し、
     * その差が0以下かどうかで順序を判断します。
     */
    @Override
    public boolean isOrderedBefore(OperationTarget other) {
        // 自分自身との比較は常にtrue (順序変更不要)
        if (this == other) {
            return true;
        }
        // 比較対象がBitwiseStateObjectインスタンスでない場合は常にtrue (不明な順序付け?)
        if (!(other instanceof BitwiseStateObject)) {
            return true;
        }
        BitwiseStateObject otherA8 = (BitwiseStateObject) other;

        // 自身のstateValueから56-63ビット目を抽出
        long selfBits = extractBitField(56, 63);
        // 比較対象のstateValueから56-63ビット目を抽出
        long otherBits = otherA8.extractBitField(56, 63);

        // 抽出したビットフィールドの値に基づいて順序を判断 (self <= other ?)
        return selfBits - otherBits <= 0;
    }

    // --- 静的ユーティリティメソッド (プールソート) ---

    /**
     * オブジェクトのハッシュコードを計算します。
     * 自身のstateValueに対して、特定のビットフィールド(0-7ビット目)の抽出結果を使用します。
     *
     * @return ハッシュコード
     */
    @Override
    public int hashCode() {
        // stateValueから0-7ビット目を抽出してintにキャスト
        return (int) extractBitField(0, 7);
    }

    /**
     * 他のオブジェクトとの等価性を比較します。
     * 自身のstateValueと他のBitwiseStateObjectのstateValueに対し、
     * 特定のビットフィールド(0-55ビット目)の抽出結果を比較します。
     *
     * @param obj 比較対象のオブジェクト
     * @return 等価であればtrue
     */
    @Override
    public boolean equals(Object obj) {
        // 同一オブジェクトならtrue
        if (this == obj) {
            return true;
        }
        // nullまたは型が異なる場合はfalse
        if (!(obj instanceof BitwiseStateObject)) {
            return false;
        }
        BitwiseStateObject otherA8 = (BitwiseStateObject) obj;

        // 自身のstateValueから0-55ビット目を抽出
        long selfBits = extractBitField(0, 55);
        // 比較対象のstateValueから0-55ビット目を抽出
        long otherBits = otherA8.extractBitField(0, 55);

        // 抽出したビットフィールドの値が等しければtrue
        return selfBits == otherBits;
    }

    /**
     * 自身のstateValueに対し、0ビット目から指定されたビット位置までのビットフィールドを抽出します。
     *
     * @param endBit 終了ビット位置 (排他的ではない、0からendBit-1までを含む)
     * @return 抽出されたビットフィールド値
     */
    private long extractBitField(int endBit) {
        return applyShiftAndMask(this.stateValue, 0, endBit - 1, this.instanceShiftMap, this.instanceBitMasks);
    }

    // --- 静的初期化メソッド ---

    /**
     * 自身のstateValueに対し、指定されたビット範囲のビットフィールドを抽出します。
     *
     * @param startBit 開始ビット位置 (0を含む)
     * @param endBit   終了ビット位置 (63を含む)
     * @return 抽出されたビットフィールド値
     */
    private long extractBitField(int startBit, int endBit) {
        return applyShiftAndMask(this.stateValue, startBit, endBit, this.instanceShiftMap, this.instanceBitMasks);
    }
}
