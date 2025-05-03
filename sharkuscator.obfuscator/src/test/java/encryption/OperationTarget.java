package encryption;

/**
 * オペレーションまたは状態の対象となるオブジェクトの共通インターフェース。
 * 様々な状態操作、関連付け、比較機能を定義します。
 */
public interface OperationTarget {
    /**
     * このオブジェクトの次に処理を委譲するオペレーションターゲットを設定します。
     *
     * @param nextTarget 次のオペレーションターゲット
     */
    void setNextOperationTarget(OperationTarget nextTarget);

    /**
     * このオブジェクトが保持するint配列の状態を返します。
     * この配列はビット操作のシフト量などに使用される場合があります。
     *
     * @return int配列の状態
     */
    int[] getIntArrayState();

    /**
     * long値を入力として受け取り、何らかの操作を実行し、結果のlong値を返します。
     * このオブジェクト自身の内部状態を更新したり、関連付けられたターゲットへ処理を委譲したりします。
     *
     * @param inputValue 入力long値
     * @return 処理結果のlong値
     */
    long processLongValue(long inputValue);

    /**
     * long値でこのオブジェクトの補助的な状態を設定します。
     *
     * @param stateValue 設定するlong値
     */
    void setLongState(long stateValue);

    /**
     * 別のOperationTargetオブジェクトと比較を行います。
     * 特にプールされたオブジェクトのソート順序を決定するために使用されます。
     *
     * @param other 比較対象のOperationTarget
     * @return このオブジェクトがotherより順序が前であればtrue
     */
    boolean isOrderedBefore(OperationTarget other);
}
