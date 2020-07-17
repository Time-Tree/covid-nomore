export const initialState = {
  messages: [],
  authToken: null,
  userId: null
};

export const ActionTypes = {
  AUTHENTICATE: 'AUTHENTICATE',
  AUTHENTICATED: 'AUTHENTICATED',
  SEND_MESSAGE: 'SEND_MESSAGE',
  MESSAGE_SENT: 'MESSAGE_SENT',
  BOT_RESPONSE_RECEIVED: 'BOT_RESPONSE_RECEIVED'
};
