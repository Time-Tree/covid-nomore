import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { GiftedChat, Bubble, Time } from 'react-native-gifted-chat';
import { View, StyleSheet, ActivityIndicator, StatusBar } from 'react-native';
import { AppLogo } from '../components';

import { getInitBotAction, getSendMessageAction } from '../redux/chat/actions';
import { getMessageFromQuickReply } from '../utils/chat';

const messagesSelector = state => state.chat.messages;
const isAuthenticatedSelector = state => state.chat.authToken != null;

const renderBubble = props => {
  const containerStyles = {
    left: styles.messageTimestampContainer,
    right: styles.messageTimestampContainer
  };

  const textStyles = {
    left: {
      ...styles.messageTimestamp,
      textAlign: 'left'
    },
    right: {
      ...styles.messageTimestamp,
      textAlign: 'right'
    }
  };

  return (
    <View>
      <Time
        {...props}
        containerStyle={containerStyles}
        timeTextStyle={textStyles}
      />
      <Bubble
        {...props}
        renderTime={() => null}
        wrapperStyle={{
          left: {
            backgroundColor: 'white',
            borderRadius: 28,
            borderTopLeftRadius: 0
          },
          right: {
            backgroundColor: '#00C0FF',
            borderRadius: 28,
            borderTopRightRadius: 0
          }
        }}
      />
    </View>
  );
};

const Chat = props => {
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

  return (
    <>
      <AppLogo
        style={{
          ...styles.logo,
          paddingTop: StatusBar.currentHeight + 20
        }}
      />
      {isAuthenticated ? (
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
          messagesContainerStyle={styles.container}
        />
      ) : (
        <View style={[styles.centered]}>
          <ActivityIndicator size="large" />
        </View>
      )}
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#f7f8fc'
  },
  centered: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100%'
  },
  quickReplyButton: {
    marginTop: 10,
    minHeight: 40
  },
  messageTimestampContainer: {
    marginBottom: 4,
    marginLeft: 0,
    marginRight: 0
  },
  messageTimestamp: {
    color: '#9BA2AB',
    fontSize: 14,
    lineHeight: 18,
    textTransform: 'uppercase'
  },
  logo: {
    paddingHorizontal: 16,
    backgroundColor: '#f7f8fc'
  }
});

export default Chat;
