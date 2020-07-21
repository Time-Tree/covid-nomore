export const formatGiftedChatMessage = rawBotResponse => {
  const { message_properties: properties } = rawBotResponse;

  const timestamp = rawBotResponse.timestamp || properties.timestamp;
  const responseOptions = JSON.parse(properties.responseOptions);
  const messageText = responseOptions.text
    ? responseOptions.text.replace(/\n/g, '\n\n')
    : '';

  const message = {
    _id: rawBotResponse.message_id || Math.round(Math.random() * 10000),
    user: {
      _id: 'bot',
      name: 'Boot'
    },
    text: messageText,
    createdAt: new Date(timestamp)
  };

  if (properties.replySuggestions.length) {
    const replyValues = properties.replySuggestions.map(reply => ({
      title: reply,
      value: reply
    }));

    message.quickReplies = {
      type: 'radio',
      keepIt: false,
      values: replyValues
    };
  }

  return message;
};

export const getMessageFromQuickReply = reply => ({
  _id: Math.round(Math.random() * 10000),
  text: reply.value,
  createdAt: new Date(),
  user: {
    _id: 1
  }
});
