package de.siphalor.amecs.mixin;

import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifier;
import de.siphalor.amecs.api.KeyModifiers;
import de.siphalor.amecs.util.IKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.controls.ControlsOptionsScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Mouse.class)
public class MixinMouse {
	@Shadow @Final private MinecraftClient client;

	@Inject(method = "onMouseScroll", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private void onMouseScroll(long window, double rawX, double rawY, CallbackInfo callbackInfo, double deltaY) {
		InputUtil.KeyCode keyCode = InputUtil.Type.MOUSE.createFromCode(deltaY > 0 ? KeyBindingUtils.MOUSE_SCROLL_DOWN : KeyBindingUtils.MOUSE_SCROLL_UP);
		if(client.currentScreen instanceof ControlsOptionsScreen) {
			KeyBinding focusedBinding = ((ControlsOptionsScreen) client.currentScreen).focusedBinding;
			if(focusedBinding != null) {
				if(((IKeyBinding) focusedBinding).amecs$getKeyCode() != InputUtil.UNKNOWN_KEYCODE) {
					KeyModifiers keyModifiers = ((IKeyBinding) focusedBinding).amecs$getKeyModifiers();
					keyModifiers.set(KeyModifier.fromKeyCode(((IKeyBinding) focusedBinding).amecs$getKeyCode().getKeyCode()), true);
				}
				client.options.setKeyCode(focusedBinding, keyCode);
				KeyBinding.updateKeysByCode();
                ((ControlsOptionsScreen) client.currentScreen).focusedBinding = null;
				callbackInfo.cancel();
				return;
			}
		}
		KeyBindingUtils.setLastScrollAmount((float) deltaY);
		KeyBinding.onKeyPressed(keyCode);
	}
}
