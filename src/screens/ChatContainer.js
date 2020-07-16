import React, { useCallback, useState, useEffect } from 'react';
import { GiftedChat } from 'react-native-gifted-chat';
import { StyleSheet } from 'react-native';

import NavbarComponent from './components/NavbarComponent';
import reduxContainer from '../redux/reduxContainer';
// import tokenActions from '../redux/tokens/actions';
// import handshakesActions from '../redux/handshakes/actions';

const ChatContainer = props => {
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    setMessages([
      {
        _id: 1,
        text: 'Hello developer',
        createdAt: new Date(),
        user: {
          _id: 2,
          name: 'React Native',
          avatar: 'https://placeimg.com/140/140/any',
        }
      }
    ]);
  }, []);

  const onSend = useCallback((messages = []) => {
    setMessages(previousMessages =>
      GiftedChat.append(previousMessages, messages)
    );
  }, []);

  return (
    <>
      <NavbarComponent title="Chat" />
      <GiftedChat
        messages={messages}
        onSend={messages => onSend(messages)}
        user={{
          _id: 1
        }}
      />
    </>
  );
};

const styles = StyleSheet.create({});

export default ChatContainer;

/* function mapStateToProps(state) {
  return {};
}
const dispatchToProps = {};

export default reduxContainer(ChatContainer, mapStateToProps, dispatchToProps); */
