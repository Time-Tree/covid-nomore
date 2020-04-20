import { combineReducers } from 'redux';
// import { store } from './store';

import settingsReducer from './settings/reducer';
import eventsReducer from './events/reducer';
import handshakesReducer from './handshakes/reducer';

const rootReducer = combineReducers({
  settings: settingsReducer,
  events: eventsReducer,
  handshakes: handshakesReducer
});

export default rootReducer;
