import logo from './logo.svg';
import './Footer.css';

function Footer() {
    return (
        <footer className="app-footer">
            <p><img src={logo} className="app-footer-logo" alt="logo"/> &copy; {new Date().getFullYear()} SmartFridge
                App. All rights reserved.</p>
        </footer>
    );
}

export default Footer;
