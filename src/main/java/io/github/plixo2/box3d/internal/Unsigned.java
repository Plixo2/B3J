package io.github.plixo2.box3d.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// When this annotation is present, treat the annotated value as unsigned.
///
/// Consider using [B3JUtil#toUnsignedInt] / [B3JUtil#toUnsignedLong]
/// to convert the value to a larger type for arithmetic or indexing.
/// Use [B3JUtil#assertU8], [B3JUtil#assertU16], or [B3JUtil#assertU32] to convert the value back.
///
///
/// @see B3JUtil#toUnsignedInt
/// @see B3JUtil#toUnsignedLong
/// @see B3JUtil#assertU8
/// @see B3JUtil#assertU16
/// @see B3JUtil#assertU32
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.RECORD_COMPONENT})
public @interface Unsigned {



}
