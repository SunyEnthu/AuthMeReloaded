package fr.xephi.authme.command.executable.unregister;

import fr.xephi.authme.data.auth.PlayerCache;
import fr.xephi.authme.message.MessageKey;
import fr.xephi.authme.process.Management;
import fr.xephi.authme.service.CommonService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link UnregisterCommand}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnregisterCommandTest {

    @InjectMocks
    private UnregisterCommand command;

    @Mock
    private Management management;

    @Mock
    private CommonService commandService;

    @Mock
    private PlayerCache playerCache;

    @Test
    public void shouldCatchUnauthenticatedUser() {
        // given
        String password = "mySecret123";
        String name = "player77";
        Player player = mock(Player.class);
        given(player.getName()).willReturn(name);
        given(playerCache.isAuthenticated(name)).willReturn(false);

        // when
        command.executeCommand(player, Collections.singletonList(password));

        // then
        verify(playerCache).isAuthenticated(name);
        verify(commandService).send(player, MessageKey.NOT_LOGGED_IN);
        verifyZeroInteractions(management);
    }

    @Test
    public void shouldForwardDataToAsyncTask() {
        // given
        String password = "p@ssw0rD";
        String name = "jas0n_";
        Player player = mock(Player.class);
        given(player.getName()).willReturn(name);
        given(playerCache.isAuthenticated(name)).willReturn(true);

        // when
        command.executeCommand(player, Collections.singletonList(password));

        // then
        verify(playerCache).isAuthenticated(name);
        verify(management).performUnregister(player, password);
    }

    @Test
    public void shouldStopIfSenderIsNotPlayer() {
        // given
        CommandSender sender = mock(CommandSender.class);

        // when
        command.executeCommand(sender, Collections.singletonList("password"));

        // then
        verifyZeroInteractions(playerCache, management);
        verify(sender).sendMessage(argThat(containsString("/authme unregister <player>")));
    }

}
