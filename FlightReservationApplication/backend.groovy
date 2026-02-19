pipeline{
    agent any 
    stages{
        stage('Code-pull'){
            steps{
                git branch: 'main', url: 'https://github.com/Sanket006/flight-reservation-app.git'
            }
        }
        stage('Build'){
            steps{
                sh '''
                    cd FlightReservationApplication
                    mvn clean package
                '''
            }
        }
        stage('QA-Test'){
            steps{
                withSonarQubeEnv(installationName: 'sonar', credentialsId: 'sonar-token') {
                  sh'''
                     cd FlightReservationApplication
                     mvn sonar:sonar -Dsonar.projectKey=Flight-Reservation-App
                  '''
                }
            }
        }
        stage('Docker-build'){
            steps{
                sh'''
                    cd FlightReservationApplication
                    docker build -t Sanket006/flight-reservation-app:latest . 
                    docker push Sanket006/flight-reservation-app:latest
                    docker rmi Sanket006/flight-reservation-app:latest
                '''
            }
        }
        stage('Deploy'){
            steps{
                sh'''
                    cd FlightReservationApplication   
                    kubectl apply -f k8s/
                '''
            }
        }

    }
}