package com.greenocean.backend;
import com.greenocean.backend.listener.live.LiveSupportSettings;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class PhaseILiveSupportSettingsTest {
 @Test void timingBoundsRejectUnsafeOperationalConfiguration(){assertThatThrownBy(()->new LiveSupportSettings(false,0,60,45,120)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->new LiveSupportSettings(true,90,6000,45,120)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->new LiveSupportSettings(true,90,60,0,120)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->new LiveSupportSettings(true,90,60,45,0)).isInstanceOf(IllegalArgumentException.class);}
 @Test void approvedDefaultsAreExplicitAndDoNotEnableService(){assertThatThrownBy(()->new LiveSupportSettings(true,90,60,45,120,30,10000)).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->new LiveSupportSettings(true,90,60,45,120,15,1)).isInstanceOf(IllegalArgumentException.class);var config=new LiveSupportSettings(false,90,60,45,120);assertThat(config.enabled()).isFalse();assertThat(config.offerSeconds()).isEqualTo(90);}
}
