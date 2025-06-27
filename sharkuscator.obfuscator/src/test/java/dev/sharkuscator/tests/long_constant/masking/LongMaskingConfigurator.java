package dev.sharkuscator.tests.long_constant.masking;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class LongMaskingConfigurator {
    private final MaskedNumber maskedNumber;

    public MaskedNumber registerAssociated(long associatedValue1, long associatedValue2) {
        maskedNumber.registerAssociated(MaskedNumber.of(associatedValue1), MaskedNumber.of(associatedValue2));
        return maskedNumber;
    }

    public MaskedNumber registerAssociated(MaskedNumber associatedValue1, MaskedNumber associatedValue2) {
        maskedNumber.registerAssociated(associatedValue1, associatedValue2);
        return maskedNumber;
    }
}
