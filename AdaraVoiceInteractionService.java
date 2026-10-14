package com.adara.assistant;
import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
public final class AdaraVoiceInteractionService extends VoiceInteractionService {
    @Override public AdaraVoiceInteractionSession onNewSession(Bundle args) { return new AdaraVoiceInteractionSession(this); }
}
