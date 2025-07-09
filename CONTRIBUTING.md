# Contributing to WisdomSpark

Thank you for considering contributing to WisdomSpark! 🎉

## 🚀 Getting Started

1. **Fork** the repository
2. **Clone** your fork locally
3. **Create a branch** for your feature/fix
4. **Make your changes**
5. **Test thoroughly**
6. **Submit a pull request**

## 📋 Development Guidelines

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistency with existing code

### Architecture
- Respect Clean Architecture principles
- Use MVVM pattern in presentation layer
- Implement proper dependency injection with Hilt
- Follow single responsibility principle

### UI/UX
- Follow Material Design 3 guidelines
- Ensure accessibility compliance (WCAG 2.1 AA)
- Test on different screen sizes
- Maintain 60fps performance

## 🧪 Testing

- Write unit tests for business logic
- Test UI components with Compose testing
- Verify accessibility features
- Test on various Android versions (API 24+)

## 📝 Pull Request Process

1. **Update documentation** if needed
2. **Add tests** for new functionality
3. **Ensure all tests pass**
4. **Update README.md** if applicable
5. **Use descriptive commit messages**

### Commit Message Format
```
type(scope): subject

body (optional)

footer (optional)
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

Example:
```
feat(quotes): add new inspirational category

- Added Business category with 10 new quotes
- Updated CategoryUtils with business emoji
- Added tests for new category functionality

Closes #123
```

## 🐛 Bug Reports

When filing an issue, please include:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Device information (model, Android version)
- Screenshots/logs if applicable

## 💡 Feature Requests

For new features, please describe:
- The problem you're trying to solve
- Your proposed solution
- Why this would be valuable to users
- Any implementation ideas

## 📚 Areas for Contribution

### High Priority
- [ ] AdMob integration improvements
- [ ] Performance optimizations
- [ ] Accessibility enhancements
- [ ] Unit test coverage

### Medium Priority
- [ ] New quote categories
- [ ] UI/UX improvements
- [ ] Internationalization (i18n)
- [ ] Widget functionality

### Low Priority
- [ ] Additional animations
- [ ] Theme customization
- [ ] Social sharing features
- [ ] Advanced analytics

## 🎯 Project Structure

```
app/src/main/java/com/albertowisdom/wisdomspark/
├── data/           # Data layer (Repository, Database, API)
├── presentation/   # UI layer (Screens, Components, ViewModels)
├── di/             # Dependency injection modules
├── utils/          # Utility classes and extensions
└── ads/            # AdMob integration
```

## ⚡ Performance Guidelines

- Avoid blocking the main thread
- Use lazy loading for large datasets
- Optimize Compose recomposition
- Minimize memory allocations
- Profile performance regularly

## 🎨 Design Guidelines

Follow the WisdomSpark design system:
- Use defined color palette (WisdomPearl, WisdomGold, etc.)
- Implement glassmorphism effects consistently
- Apply spring animations for interactions
- Maintain visual hierarchy

## 📞 Questions?

- Open a [Discussion](https://github.com/chopinmtnez/WisdomSpark/discussions)
- Create an [Issue](https://github.com/chopinmtnez/WisdomSpark/issues)
- Check existing documentation

## 🏆 Recognition

Contributors will be acknowledged in:
- README.md Contributors section
- Release notes
- App credits (for significant contributions)

Thank you for helping make WisdomSpark better! 🌟
