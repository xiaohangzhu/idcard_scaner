/*    */ package com.turui.bank.ocr.card;
/*    */ 
/*    */

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/*    */
/*    */
/*    */ 
/*    */ public class BeanUtils
/*    */ {
/*    */   public static void setFieldValue(Object object, String fieldName, Object value)
/*    */   {
/* 17 */     Field field = getDeclaredField(object, fieldName);
/* 18 */     if (field == null)
/* 19 */       throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
/* 20 */     makeAccessible(field);
/*    */     try {
/* 22 */       field.set(object, value);
/*    */     } catch (IllegalAccessException e) {
/* 24 */       Log.e("zbkc", "", e);
/*    */     }
/*    */   }
/*    */ 
/*    */   protected static Field getDeclaredField(Object object, String fieldName)
/*    */   {
/* 32 */     return getDeclaredField(object.getClass(), fieldName);
/*    */   }
/*    */ 
/*    */   protected static Field getDeclaredField(Class clazz, String fieldName)
/*    */   {
/* 40 */     for (Class superClass = clazz; superClass != Object.class; ) {
/*    */       try {
/* 42 */         return superClass.getDeclaredField(fieldName);
/*    */       }
/*    */       catch (NoSuchFieldException localNoSuchFieldException)
/*    */       {
/* 40 */         superClass = superClass.getSuperclass();
/*    */       }
/*    */ 
/*    */     }
/*    */ 
/* 47 */     return null;
/*    */   }
/*    */ 
/*    */   protected static void makeAccessible(Field field)
/*    */   {
/* 54 */     if ((!Modifier.isPublic(field.getModifiers())) || (!Modifier.isPublic(field.getDeclaringClass().getModifiers())))
/* 55 */       field.setAccessible(true);
/*    */   }
/*    */ }

/* Location:           F:\多引擎\IDC_OCR_SYSTEM_A\Build\NEW_SDK\Android_SDK\TCardApi.jar
 * Qualified Name:     com.ui.card.BeanUtils
 * JD-Core Version:    0.6.2
 */