export const initialState = {
  tokens: [],
  sync: -1,
  get_tokens_pending: false,
  get_tokens_error: null,
  delete_tokens_pending: false,
  delete_tokens_error: null,
  send_tokens_pending: false,
  send_tokens_error: null
};

export const ActionTypes = {
  ADD_TOKEN: 'ADD_TOKEN',
  CLEAR_TOKENS: 'CLEAR_TOKENS',
  SET_SYNC: 'SET_SYNC',
  GET_TOKENS: 'GET_TOKENS',
  GET_TOKENS_SUCCESS: 'GET_TOKENS_SUCCESS',
  GET_TOKENS_FAILED: 'GET_TOKENS_FAILED',
  DELETE_TOKENS: 'DELETE_TOKENS',
  DELETE_TOKENS_SUCCESS: 'DELETE_TOKENS_SUCCESS',
  DELETE_TOKENS_FAILED: 'DELETE_TOKENS_FAILED',
  SEND_TOKENS: 'SEND_TOKENS',
  SEND_TOKENS_SUCCESS: 'SEND_TOKENS_SUCCESS',
  SEND_TOKENS_FAILED: 'SEND_TOKENS_FAILED'
};
