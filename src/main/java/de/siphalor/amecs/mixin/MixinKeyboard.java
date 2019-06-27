package de.siphalor.amecs.mixin;

import de.siphalor.amecs.impl.KeyBindingManager;
import de.siphalor.amecs.util.StaticContainer;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.controls.ControlsOptionsScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.SystemUtil;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
	@Inject(method = "onKey", at = @At("HEAD"))
	private void onKeyInControlsOptionsScreen(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		// Key released
		if(int_3 == 0 && MinecraftClient.getInstance().currentScreen instanceof ControlsOptionsScreen) {
			ControlsOptionsScreen screen = (ControlsOptionsScreen) MinecraftClient.getInstance().currentScreen;

			screen.focusedBinding = null;
            screen.time = SystemUtil.getMeasuringTimeMs();
		}
	}

	@Inject(
		method = "onKey",
		at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;Z)V"),
			@At(value = "INVOKE", target = "Lnet/minecraft/client/options/KeyBinding;onKeyPressed(Lnet/minecraft/client/util/InputUtil$KeyCode;)V")
		}
		)
	private void onKeyBindingMethod(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		StaticContainer.ignoreKeyBindingMethod = true;
	}

	@Inject(method = "onKey", at = @At(value = "JUMP", ordinal = 0, opcode = Opcodes.IFNULL), cancellable = true)
	private void triggerKeyBindings(long window, int int_1, int int_2, int int_3, int int_4, CallbackInfo callbackInfo) {
		InputUtil.KeyCode keyCode = InputUtil.getKeyCode(int_1, int_2);
		if(int_3 == 0) {
			KeyBindingManager.setKeyPressed(keyCode, false);
		} else {
			KeyBindingManager.setKeyPressed(keyCode, true);
			KeyBindingManager.onKeyPressed(keyCode);
		}
	}
}
