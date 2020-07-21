export const initialState = {
  messages: [],
  authToken: null,
  userId: null,
  latestInit: null
};

export const ActionTypes = {
  AUTHENTICATE: 'AUTHENTICATE',
  AUTHENTICATED: 'AUTHENTICATED',
  INIT_BOT: 'INIT_BOT',
  BOT_INIT_STARTED: 'BOT_INIT_STARTED',
  SEND_MESSAGE: 'SEND_MESSAGE',
  MESSAGE_SENT: 'MESSAGE_SENT',
  BOT_RESPONSE_RECEIVED: 'BOT_RESPONSE_RECEIVED'
};
