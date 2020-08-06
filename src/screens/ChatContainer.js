import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { GiftedChat, Bubble } from 'react-native-gifted-chat';
import { View, StyleSheet, ActivityIndicator } from 'react-native';

import NavbarComponent from './components/NavbarComponent';
import { getInitBotAction, getSendMessageAction } from '../redux/chat/actions';
import { getMessageFromQuickReply } from '../utils/chat';

const messagesSelector = state => state.chat.messages;
const isAuthenticatedSelector = state => state.chat.authToken != null;

const renderBubble = props => (
  <Bubble
    {...props}
    wrapperStyle={{
      left: {
        backgroundColor: 'white'
      }
    }}
  />
);

const ChatContainer = props => {
  const messages = useSelector(messagesSelector);
  const isAuthenticated = useSelector(isAuthenticatedSelector);

  const dispatch = useDispatch();
  const sendMessages = newMessages =>
    newMessages.forEach(message => dispatch(getSendMessageAction(message)));

  const sendQuickReplies = replies =>
    replies.forEach(reply => {
      const message = getMessageFromQuickReply(reply);
      dispatch(getSendMessageAction(message));
    });

  useEffect(() => {
    dispatch(getInitBotAction());
  });

  const ChatComponent = (
    <GiftedChat
      messages={messages}
      onSend={sendMessages}
      user={{
        _id: 1
      }}
      renderAvatar={null}
      renderBubble={renderBubble}
      quickReplyStyle={styles.quickReplyButton}
      onQuickReply={sendQuickReplies}
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
  },
  quickReplyButton: {
    marginTop: 10,
    minHeight: 40
  }
});

export default ChatContainer;
