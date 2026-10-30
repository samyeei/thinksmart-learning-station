package local.thinksmart.manualkeyboard;

import org.junit.Test;
import static org.junit.Assert.*;

public class KeyboardPolicyTest {
 @Test public void entryIsCollapsedAndExplicitToggleOpens() {
  KeyboardPolicy p=new KeyboardPolicy();p.enter();assertFalse(p.expanded());
  p.toggle(1000);assertTrue(p.expanded());p.toggle(1100);assertFalse(p.expanded());
 }
 @Test public void newEditorResetsButImmediateImeRebindDoesNot() {
  KeyboardPolicy p=new KeyboardPolicy();p.enter();p.toggle(1000);
  p.editorFocused(1100);assertTrue(p.expanded());
  p.editorFocused(2500);assertFalse(p.expanded());
 }
 @Test public void LeavingAndScreenOffForgetManualPermission() {
  KeyboardPolicy p=new KeyboardPolicy();p.enter();p.toggle(1000);p.leave();assertFalse(p.expanded());assertFalse(p.active());
  p.enter();assertFalse(p.expanded());p.toggle(2000);p.screenOff();assertFalse(p.expanded());
 }
 @Test public void RepeatedForegroundEventsDoNotCloseTyping() {
  KeyboardPolicy p=new KeyboardPolicy();p.enter();p.toggle(1000);p.enter();assertTrue(p.expanded());
 }
 @Test public void ButtonsOutsideAppCannotAuthorizeKeyboard() {
  KeyboardPolicy p=new KeyboardPolicy();p.toggle(1000);assertFalse(p.expanded());
 }
 @Test public void SameEditorTypingStaysOpenButDifferentEditorCloses() {
  KeyboardPolicy p=new KeyboardPolicy();p.enter();p.trackEditor(42);p.toggle(1000);
  p.observeEditor(42,3000);assertTrue(p.expanded());
  p.observeEditor(43,3100);assertFalse(p.expanded());
 }
 @Test public void LosingEditorClosesAndRebindHasShortGrace() {
  KeyboardPolicy p=new KeyboardPolicy();p.enter();p.trackEditor(42);p.toggle(1000);
  p.observeEditor(0,1100);assertTrue(p.expanded());
  p.observeEditor(0,2100);assertFalse(p.expanded());
 }
}
