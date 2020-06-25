package com.runewatch;

import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.MessageNode;
import net.runelite.api.events.FriendsChatMemberJoined;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@PluginDescriptor(
		name = "RuneWatch Checker",
		description = "Checks everyone who joins a friends chat for their RuneWatch status",
		tags = {"rw", "fc", "clan"}
)
@Slf4j

public class RuneWatchPlugin extends Plugin
{
	private String RuneWatch_URL = "http://www.runewatch.com/api/cases/";

	@Inject
	ChatMessageManager chatMessageManager;


	@Inject
	Client client;

//    @Inject
//    RuneWatchConfig config;

	@Subscribe
	public void onFriendsChatMemberJoined(FriendsChatMemberJoined event) throws IOException
	{
		FriendsChatMember member = event.getMember();
		String name = member.getName();
		name = name.replace("\u00A0", "%20");
		log.debug(name);
		URL RW_Connection_String = new URL(null,  RuneWatch_URL + name,
				new sun.net.www.protocol.https.Handler());
		log.debug(RW_Connection_String.toString());
		HttpsURLConnection conn = (HttpsURLConnection) RW_Connection_String.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		BufferedReader in = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null)
		{
			content.append(inputLine);
		}
		log.debug(content.toString());
		String response;
		ChatMessageBuilder responseCMB = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append(member.getName());
		if (content.toString().contains("[]"))
		{
			response = responseCMB
					.append(" Does not appear on RuneWatch")
					.build();
		} else
		{
			response = responseCMB
					.append(ChatColorType.HIGHLIGHT)
					.append(" Does ")
					.append(ChatColorType.NORMAL)
					.append(" appear on RuneWatch")
					.build();
		}

		client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", response, "RWC:");

	}


}
