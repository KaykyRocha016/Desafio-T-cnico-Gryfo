# Desafio-T-cnico-Gryfo
esse repositório contém os códigos desenvolvidos por mim durante a realização do desafio técnico Gryfo

# Documentação Técnica do Projeto de Aplicação Android

## Visão Geral
Este projeto é uma aplicação Android que tira duas fotos e as envia para um endpoint. O projeto é composto por três classes principais: `MainActivity`, `PhotoBottomSheetContent` e `CameraPreview`.

## MainActivity
A `MainActivity` é a classe principal da aplicação. Ela herda de `ComponentActivity`, que é uma classe de atividade leve projetada para uso com o Jetpack Compose.

### onCreate
O método `onCreate` é chamado quando a atividade é iniciada. Ele verifica se a aplicação tem as permissões necessárias para acessar a câmera e o áudio. Se não tiver, solicita essas permissões ao usuário.

### setContent
Aqui estamos definindo o conteúdo da UI da nossa atividade. Estamos usando o tema `LearningCameraXTheme`, que provavelmente é um tema personalizado definido em algum lugar do seu projeto.

### takeAPhoto
Este método é usado para tirar uma foto usando a câmera. Ele usa o `LifecycleCameraController` para tirar a foto e, em seguida, roda a imagem para a orientação correta.

## PhotoBottomSheetContent
Esta é uma função `@Composable` que é usada para exibir uma lista de imagens em um `BottomSheet` na sua aplicação. Se a lista de bitmaps estiver vazia, ela exibirá uma mensagem dizendo "There are no photos yet". Se a lista de bitmaps não estiver vazia, ela exibirá as imagens em um `LazyVerticalStaggeredGrid`.

## CameraPreview
Esta é uma função `@Composable` que é usada para exibir a pré-visualização da câmera na UI. Ela usa o `LifecycleCameraController` para vincular o ciclo de vida da câmera ao ciclo de vida do proprietário do ciclo de vida local.

## MainViewModel
Esta é a classe ViewModel para a lógica da câmera e a comunicação com o servidor. Ela tem um `StateFlow` para armazenar as imagens capturadas e a resposta do servidor. Quando uma foto é capturada, ela é adicionada ao `StateFlow` das imagens. Se duas imagens foram capturadas, elas são enviadas para o servidor e o `StateFlow` das imagens é limpo.

### onTakePhoto
Esta função é chamada quando uma foto é capturada. Ela adiciona a imagem ao `StateFlow` das imagens e, se duas imagens foram capturadas, envia as imagens para o servidor.

### sendPhotosToEndpointAsync
Esta função é usada para enviar as imagens capturadas para o servidor de forma assíncrona. Ela usa o `OkHttpClient` para fazer a requisição ao servidor.

Espero que isso ajude a entender melhor o seu projeto! Se você tiver mais perguntas ou precisar de mais detalhes, fique à vontade para perguntar. 😊
