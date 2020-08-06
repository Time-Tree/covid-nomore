import { combineReducers } from 'redux';
// import { store } from './store';

import settingsReducer from './settings/reducer';
import eventsReducer from './events/reducer';
import handshakesReducer from './handshakes/reducer';
import tokensReducer from './tokens/reducer';
import chatReducer from './chat/reducer';

const rootReducer = combineReducers({
  settings: settingsReducer,
  events: eventsReducer,
  handshakes: handshakesReducer,
  tokens: tokensReducer,
  chat: chatReducer
});

export default rootReducer;
