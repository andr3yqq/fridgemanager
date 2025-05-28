import logo from './logo.svg';
import './Header.css';

function Header() {
    return (
        <header className="app-header">
            <img src={logo} className="app-header-logo" alt="logo"/>
            <div className="app-header-title">SmartFridge App</div>
            </header>
    );
}

export default Header;
