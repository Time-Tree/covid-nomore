import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { GiftedChat } from 'react-native-gifted-chat';
import { View, StyleSheet, ActivityIndicator } from 'react-native';

import NavbarComponent from './components/NavbarComponent';
import { getAuthAction, getSendMessageAction } from '../redux/chat/actions';
import { WiseVoiceKey } from '../../keys';

const messagesSelector = state => state.chat.messages;
const isAuthenticatedSelector = state => state.chat.authToken != null;

const ChatContainer = props => {
  const messages = useSelector(messagesSelector);
  const isAuthenticated = useSelector(isAuthenticatedSelector);

  const dispatch = useDispatch();
  const sendMessage = message => dispatch(getSendMessageAction(message));

  useEffect(() => {
    if (!isAuthenticated) {
      dispatch(
        getAuthAction(
          WiseVoiceKey.projectId,
          WiseVoiceKey.clientKey,
          WiseVoiceKey.channel
        )
      );
    }
  });

  const ChatComponent = (
    <GiftedChat
      messages={messages}
      onSend={sendMessage}
      user={{
        _id: 1
      }}
    />
  );

  return (
    <>
      <NavbarComponent title="Chat" />
      {isAuthenticated ? (
        ChatComponent
      ) : (
        <View style={[styles.centered]}>
          <ActivityIndicator size="large" />
        </View>
      )}
    </>
  );
};

const styles = StyleSheet.create({
  centered: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100%'
  }
});

export default ChatContainer;
